package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.ReservaRequest;
import com.example.onboarding.dto.request.SesionRequest;
import com.example.onboarding.dto.request.SesionSoporteRequest;
import com.example.onboarding.dto.request.SolicitudSesionRequest;
import com.example.onboarding.dto.response.SesionResponse;
import com.example.onboarding.dto.response.SolicitudSesionResponse;
import com.example.onboarding.entity.SesionSeguimiento;
import com.example.onboarding.entity.SolicitudSesion;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.EstadoSesion;
import com.example.onboarding.enums.EstadoSolicitud;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.exception.AccesoDenegadoException;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.integration.TeamsClient;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.SesionSeguimientoRepository;
import com.example.onboarding.repository.SolicitudSesionRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.ReservaService;
import com.example.onboarding.service.SesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionServiceImpl implements SesionService {

    /** Máximo de sesiones por empleado, sin contar las canceladas. */
    private static final int MAX_SESIONES = 10;

    private final SesionSeguimientoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudSesionRepository solicitudRepository;
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
            throw new ReglaNegocioException("MAX_SESIONES", "El empleado ya tiene el máximo de " + MAX_SESIONES + " sesiones");
        }
        verificarDisponibilidad(managerId, request.fechaHora());

        SesionSeguimiento sesion = SesionSeguimiento.builder()
                .manager(manager)
                .empleado(empleado)
                .fechaHora(request.fechaHora())
                .estado(EstadoSesion.PROGRAMADA)
                .asunto(asuntoDe(request.asunto(), null))
                .descripcion(limpiar(request.descripcion()))
                .duracionMinutos(request.duracionMinutos() != null
                        ? request.duracionMinutos() : SesionSeguimiento.DURACION_POR_DEFECTO)
                .build();
        sesion.setEnlaceTeams(crearReunion(sesion));
        sesion = sesionRepository.save(sesion);

        reservarEspacio(managerId, sesion, request.espacioId());
        // Las solicitudes pendientes del empleado quedan atendidas con esta sesión.
        solicitudRepository.findByEmpleadoIdAndEstado(empleado.getId(), EstadoSolicitud.PENDIENTE)
                .forEach(s -> s.setEstado(EstadoSolicitud.ATENDIDA));
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
        sesion.setAsunto(asuntoDe(request.asunto(), sesion.getAsunto()));
        if (request.descripcion() != null) {
            sesion.setDescripcion(limpiar(request.descripcion()));
        }
        if (request.duracionMinutos() != null) {
            sesion.setDuracionMinutos(request.duracionMinutos());
        }
        sesion.setEnlaceTeams(crearReunion(sesion));
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
            throw new ReglaNegocioException("SESION_FUTURA", "No se puede marcar como realizada una sesión que aún no ocurre");
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

        sesion.setSoporteUrl(request.soporteUrl().trim());
        sesion.setObservaciones(limpiar(request.observaciones()));
        sesion.setFechaVerificacion(LocalDateTime.now());
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

    @Override
    @Transactional
    public SolicitudSesionResponse solicitar(Long empleadoId, SolicitudSesionRequest request) {
        Usuario empleado = buscarUsuario(empleadoId);
        if (empleado.getManager() == null) {
            throw new ReglaNegocioException("SIN_MANAGER", "Aún no tienes un manager asignado");
        }
        if (solicitudRepository.existsByEmpleadoIdAndEstado(empleadoId, EstadoSolicitud.PENDIENTE)) {
            throw new ReglaNegocioException("SOLICITUD_PENDIENTE", "Ya tienes una solicitud pendiente con tu manager");
        }
        if (request.fechaPropuesta() != null && request.fechaPropuesta().isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha propuesta debe ser futura");
        }
        SolicitudSesion solicitud = solicitudRepository.save(SolicitudSesion.builder()
                .empleado(empleado)
                .manager(empleado.getManager())
                .fechaPropuesta(request.fechaPropuesta())
                .comentario(limpiar(request.comentario()))
                .build());
        auditoriaService.registrar(empleadoId, AccionAuditoria.SESION_SOLICITADA, "SolicitudSesion",
                solicitud.getId(), "Solicitud de sesión para " + empleado.getManager().getNombre());
        return mapper.toSolicitudResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudSesionResponse> solicitudesPendientes(Long managerId) {
        return solicitudRepository.findByManagerIdAndEstadoOrderByFechaDesc(managerId, EstadoSolicitud.PENDIENTE)
                .stream().map(mapper::toSolicitudResponse).toList();
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

    private String crearReunion(SesionSeguimiento sesion) {
        return teamsClient.crearReunion(sesion.getAsunto() + ": " + sesion.getEmpleado().getNombre(),
                sesion.getFechaHora(), sesion.fechaFin(),
                sesion.getManager().getCorreo(), sesion.getEmpleado().getCorreo());
    }

    private void reservarEspacio(Long usuarioId, SesionSeguimiento sesion, Long espacioId) {
        if (espacioId != null) {
            reservaService.crear(usuarioId, new ReservaRequest(
                    espacioId, sesion.getId(), sesion.getFechaHora(), sesion.fechaFin()));
        }
    }

    private String asuntoDe(String pedido, String actual) {
        if (pedido != null && !pedido.isBlank()) {
            return pedido.trim();
        }
        return actual != null ? actual : SesionSeguimiento.ASUNTO_POR_DEFECTO;
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
