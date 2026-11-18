package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.ReservaRequest;
import com.example.onboarding.dto.response.FranjaResponse;
import com.example.onboarding.dto.response.ReservaResponse;
import com.example.onboarding.entity.EspacioFisico;
import com.example.onboarding.entity.Reserva;
import com.example.onboarding.entity.SesionSeguimiento;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.EspacioFisicoRepository;
import com.example.onboarding.repository.ReservaRepository;
import com.example.onboarding.repository.SesionSeguimientoRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.ReservaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final SesionSeguimientoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final int minutosFranja;

    public ReservaServiceImpl(ReservaRepository reservaRepository, EspacioFisicoRepository espacioRepository,
                              SesionSeguimientoRepository sesionRepository, UsuarioRepository usuarioRepository,
                              AuditoriaService auditoriaService, EntityMapper mapper,
                              @Value("${onboarding.reservas.hora-inicio:08:00}") String horaInicio,
                              @Value("${onboarding.reservas.hora-fin:18:00}") String horaFin,
                              @Value("${onboarding.reservas.franja-minutos:30}") int minutosFranja) {
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.mapper = mapper;
        this.horaInicio = LocalTime.parse(horaInicio);
        this.horaFin = LocalTime.parse(horaFin);
        this.minutosFranja = minutosFranja;
    }

    @Override
    @Transactional
    public ReservaResponse crear(Long usuarioId, ReservaRequest request) {
        if (!request.fin().isAfter(request.inicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la de inicio");
        }
        EspacioFisico espacio = espacioRepository.findById(request.espacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado: " + request.espacioId()));
        if (!espacio.getActivo()) {
            throw new ReglaNegocioException("El espacio no está disponible para reservas");
        }
        if (reservaRepository.existeCruce(espacio.getId(), request.inicio(), request.fin())) {
            throw new ReglaNegocioException("ESPACIO_OCUPADO", "El espacio ya está reservado en ese horario");
        }

        SesionSeguimiento sesion = null;
        if (request.sesionId() != null) {
            sesion = sesionRepository.findById(request.sesionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada: " + request.sesionId()));
            if (reservaRepository.findBySesionId(sesion.getId()).isPresent()) {
                throw new ReglaNegocioException("La sesión ya tiene un espacio reservado");
            }
        }

        Reserva reserva = Reserva.builder()
                .espacio(espacio)
                .sesion(sesion)
                .reservadoPor(usuarioRepository.getReferenceById(usuarioId))
                .inicio(request.inicio())
                .fin(request.fin())
                .build();
        reserva = reservaRepository.save(reserva);

        auditoriaService.registrar(usuarioId, AccionAuditoria.RESERVA_CREADA, "Reserva", reserva.getId(),
                "Espacio " + espacio.getNombre() + " reservado desde " + request.inicio());
        return mapper.toReservaResponse(reservaRepository.findById(reserva.getId()).orElse(reserva));
    }

    @Override
    @Transactional
    public void liberarPorSesion(Long sesionId) {
        reservaRepository.findBySesionId(sesionId).ifPresent(r -> {
            reservaRepository.delete(r);
            reservaRepository.flush();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findDelUsuarioOSusSesiones(usuarioId).stream()
                .map(mapper::toReservaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorEspacio(Long espacioId, LocalDateTime desde, LocalDateTime hasta) {
        return reservaRepository.findByEspacioIdAndInicioBetweenOrderByInicioAsc(espacioId, desde, hasta).stream()
                .map(mapper::toReservaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FranjaResponse> franjasDelDia(Long espacioId, LocalDate fecha) {
        espacioRepository.findById(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio no encontrado: " + espacioId));
        LocalDateTime desde = fecha.atTime(horaInicio);
        LocalDateTime hasta = fecha.atTime(horaFin);
        List<Reserva> ocupadas = reservaRepository.findCruzadas(espacioId, desde, hasta);
        LocalDateTime ahora = LocalDateTime.now();

        List<FranjaResponse> franjas = new ArrayList<>();
        for (LocalDateTime inicio = desde; inicio.isBefore(hasta); inicio = inicio.plusMinutes(minutosFranja)) {
            LocalDateTime fin = inicio.plusMinutes(minutosFranja);
            final LocalDateTime i = inicio;
            Reserva cruce = ocupadas.stream()
                    .filter(r -> r.getInicio().isBefore(fin) && r.getFin().isAfter(i))
                    .findFirst().orElse(null);
            if (cruce != null) {
                SesionSeguimiento s = cruce.getSesion();
                franjas.add(new FranjaResponse(inicio, fin, false, cruce.getReservadoPor().getNombre(),
                        s != null ? (s.getAsunto() != null ? s.getAsunto() : SesionSeguimiento.ASUNTO_POR_DEFECTO) : null));
            } else {
                franjas.add(new FranjaResponse(inicio, fin, inicio.isAfter(ahora), null, null));
            }
        }
        return franjas;
    }
}
