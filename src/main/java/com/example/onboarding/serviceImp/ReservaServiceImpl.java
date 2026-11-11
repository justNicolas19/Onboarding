package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.ReservaRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {


    private final ReservaRepository reservaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final SesionSeguimientoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

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
            throw new ReglaNegocioException("El espacio ya está reservado en ese horario");
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
        return mapper.toReservaResponse(reserva);
    }

    @Override
    @Transactional
    public void liberarPorSesion(Long sesionId) {
        reservaRepository.findBySesionId(sesionId).ifPresent(reservaRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findByReservadoPorIdOrderByInicioDesc(usuarioId).stream()
                .map(mapper::toReservaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorEspacio(Long espacioId, LocalDateTime desde, LocalDateTime hasta) {
        return reservaRepository.findByEspacioIdAndInicioBetweenOrderByInicioAsc(espacioId, desde, hasta).stream()
                .map(mapper::toReservaResponse).toList();
    }
}
