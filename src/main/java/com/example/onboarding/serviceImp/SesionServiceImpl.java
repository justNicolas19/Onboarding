package com.example.onboarding.serviceImp;



import com.example.onboarding.dto.request.ReservaRequest;
import com.example.onboarding.dto.request.SesionRequest;
import com.example.onboarding.dto.request.SesionSoporteRequest;
import com.example.onboarding.dto.response.SesionResponse;
import com.example.onboarding.entity.SesionSeguimiento;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.EstadoSesion;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.exception.AccesoDenegadoException;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.integration.TeamsClient;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.SesionSeguimientoRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.ReservaService;
import com.example.onboarding.service.SesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionServiceImpl implements SesionService {


    /** Máximo de sesiones por empleado, sin contar las canceladas. */
    private static final int MAX_SESIONES = 10;
    /** Duración supuesta de cada sesión, usada para Teams y para reservar el espacio. */
    private static final Duration DURACION = Duration.ofHours(1);

    private final SesionSeguimientoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaService reservaService;
    private final TeamsClient teamsClient;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public SesionResponse agendar(Long managerId, SesionRequest request) {
        Usuario manager = buscarUsuario(managerId);
        if (manager.getRol() != Rol.MANAGER) {
            throw new AccesoDenegadoException("Solo un manager puede agendar sesiones");
        }
        Usuario empleado = buscarUsuario(request.empleadoId());
        verificarACargo(manager, empleado);

        long vigentes = sesionRepository.countByEmpleadoIdAndEstadoNot(empleado.getId(), EstadoSesion.CANCELADA);
        if (vigentes >= MAX_SESIONES) {
            throw new ReglaNegocioException("El empleado ya tiene el máximo de " + MAX_SESIONES + " sesiones");
        }
        verificarDisponibilidad(managerId, request.fechaHora());

        SesionSeguimiento sesion = SesionSeguimiento.builder()
                .manager(manager)
                .empleado(empleado)
                .fechaHora(request.fechaHora())
                .estado(EstadoSesion.PROGRAMADA)
                .enlaceTeams(crearReunion(manager, empleado, request.fechaHora()))
                .build();
        sesion = sesionRepository.save(sesion);

        reservarEspacio(managerId, sesion, request.espacioId());
        auditoriaService.registrar(managerId, AccionAuditoria.SESION_AGENDADA, "SesionSeguimiento", sesion.getId(),
                "Sesión con " + empleado.getNombre() + " el " + sesion.getFechaHora());
        return mapper.toSesionResponse(sesion);
    }

    @Override
    @Transactional
    public SesionResponse reprogramar(Long managerId, Long sesionId, SesionRequest request) {
        SesionSeguimiento sesion = buscarSesion(sesionId);
        verificarPropietario(managerId, sesion);
        exigirEstado(sesion, EstadoSesion.PROGRAMADA, "Solo se puede reprogramar una sesión programada");
        if (!sesion.getEmpleado().getId().equals(request.empleadoId())) {
            throw new ReglaNegocioException("No se puede cambiar el empleado; cancela la sesión y agenda otra");
        }
        if (!sesion.getFechaHora().equals(request.fechaHora())) {
            verificarDisponibilidad(managerId, request.fechaHora());
        }

        sesion.setFechaHora(request.fechaHora());
        sesion.setEnlaceTeams(crearReunion(sesion.getManager(), sesion.getEmpleado(), request.fechaHora()));
        sesion = sesionRepository.save(sesion);

        reservaService.liberarPorSesion(sesionId);
        reservarEspacio(managerId, sesion, request.espacioId());
        auditoriaService.registrar(managerId, AccionAuditoria.SESION_REPROGRAMADA, "SesionSeguimiento", sesionId,
                "Nueva fecha: " + sesion.getFechaHora());
        return mapper.toSesionResponse(sesion);
    }

    @Override
    @Transactional
    public SesionResponse cancelar(Long managerId, Long sesionId) {
        SesionSeguimiento sesion = buscarSesion(sesionId);
        verificarPropietario(managerId, sesion);
        exigirEstado(sesion, EstadoSesion.PROGRAMADA, "Solo se puede cancelar una sesión programada");

        sesion.setEstado(EstadoSesion.CANCELADA);
        sesion = sesionRepository.save(sesion);
        reservaService.liberarPorSesion(sesionId);
        auditoriaService.registrar(managerId, AccionAuditoria.SESION_CANCELADA, "SesionSeguimiento", sesionId,
                "Sesión cancelada");
        return mapper.toSesionResponse(sesion);
    }

    @Override
    @Transactional
    public SesionResponse marcarRealizada(Long managerId, Long sesionId) {
        SesionSeguimiento sesion = buscarSesion(sesionId);
        verificarPropietario(managerId, sesion);
        exigirEstado(sesion, EstadoSesion.PROGRAMADA, "Solo se puede marcar como realizada una sesión programada");
        if (sesion.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se puede marcar como realizada una sesión que aún no ocurre");
        }

        sesion.setEstado(EstadoSesion.REALIZADA);
        sesion = sesionRepository.save(sesion);
        auditoriaService.registrar(managerId, AccionAuditoria.SESION_REALIZADA, "SesionSeguimiento", sesionId,
                "Sesión realizada");
        return mapper.toSesionResponse(sesion);
    }

    @Override
    @Transactional
    public SesionResponse adjuntarSoporte(Long managerId, Long sesionId, SesionSoporteRequest request) {
        SesionSeguimiento sesion = buscarSesion(sesionId);
        verificarPropietario(managerId, sesion);
        exigirEstado(sesion, EstadoSesion.REALIZADA, "El soporte se adjunta a una sesión ya realizada");

        sesion.setSoporteUrl(request.soporteUrl());
        sesion.setEstado(EstadoSesion.VERIFICADA);
        sesion = sesionRepository.save(sesion);
        auditoriaService.registrar(managerId, AccionAuditoria.SESION_VERIFICADA, "SesionSeguimiento", sesionId,
                "Soporte adjuntado");
        return mapper.toSesionResponse(sesion);
    }

    @Override
    @Transactional(readOnly = true)
    public SesionResponse obtener(Long sesionId) {
        return mapper.toSesionResponse(buscarSesion(sesionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionResponse> listarPorManager(Long managerId) {
        return sesionRepository.findByManagerIdOrderByFechaHoraDesc(managerId).stream()
                .map(mapper::toSesionResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionResponse> listarPorEmpleado(Long empleadoId) {
        return sesionRepository.findByEmpleadoIdOrderByFechaHoraDesc(empleadoId).stream()
                .map(mapper::toSesionResponse).toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    private SesionSeguimiento buscarSesion(Long id) {
        return sesionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión no encontrada: " + id));
    }

    private void verificarACargo(Usuario manager, Usuario empleado) {
        if (empleado.getRol() != Rol.EMPLEADO
                || empleado.getManager() == null
                || !empleado.getManager().getId().equals(manager.getId())) {
            throw new AccesoDenegadoException("El empleado no está a tu cargo");
        }
    }

    private void verificarPropietario(Long managerId, SesionSeguimiento sesion) {
        if (!sesion.getManager().getId().equals(managerId)) {
            throw new AccesoDenegadoException("La sesión pertenece a otro manager");
        }
    }

    private void exigirEstado(SesionSeguimiento sesion, EstadoSesion esperado, String mensaje) {
        if (sesion.getEstado() != esperado) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private void verificarDisponibilidad(Long managerId, LocalDateTime fechaHora) {
        if (sesionRepository.existsByManagerIdAndFechaHoraAndEstado(managerId, fechaHora, EstadoSesion.PROGRAMADA)) {
            throw new ReglaNegocioException("Ya tienes una sesión programada a esa hora");
        }
    }

    private String crearReunion(Usuario manager, Usuario empleado, LocalDateTime inicio) {
        return teamsClient.crearReunion("Seguimiento de onboarding: " + empleado.getNombre(),
                inicio, inicio.plus(DURACION), manager.getCorreo(), empleado.getCorreo());
    }

    private void reservarEspacio(Long usuarioId, SesionSeguimiento sesion, Long espacioId) {
        if (espacioId != null) {
            reservaService.crear(usuarioId, new ReservaRequest(
                    espacioId, sesion.getId(), sesion.getFechaHora(), sesion.getFechaHora().plus(DURACION)));
        }
    }
}
