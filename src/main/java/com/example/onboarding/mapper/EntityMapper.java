package com.example.onboarding.mapper;

import com.example.onboarding.dto.response.*;
import com.example.onboarding.entity.*;
import com.example.onboarding.repository.ReservaRepository;
import com.example.onboarding.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Convierte entities en DTOs de respuesta. Debe usarse dentro de una transacción (relaciones lazy). */
@Component
public class EntityMapper {

    /** Una alerta se considera activa durante 7 días. */
    public static final int DIAS_ALERTA_ACTIVA = 7;

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final String plantillaUrlMoodle;

    public EntityMapper(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository,
                        @Value("${onboarding.moodle.url-actividad:}") String plantillaUrlMoodle) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.plantillaUrlMoodle = plantillaUrlMoodle;
    }

    public UsuarioResponse toUsuarioResponse(Usuario u) {
        Usuario m = u.getManager();
        Ruta r = u.getRuta();
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getCargo(),
                u.getFechaIngreso(), u.getActivo(),
                m != null ? m.getId() : null, m != null ? m.getNombre() : null,
                r != null ? r.getId() : null, r != null ? r.getNombre() : null,
                u.getIdMoodle(), u.getArea(), u.getPresentacion(), u.getFotoUrl());
    }

    public LiderResponse toLiderResponse(Usuario lider, Long managerDelEmpleadoId) {
        return new LiderResponse(lider.getId(), lider.getNombre(), lider.getCorreo(), lider.getCargo(),
                lider.getArea(), lider.getPresentacion(), lider.getFotoUrl(),
                lider.getId().equals(managerDelEmpleadoId));
    }

    public ModuloResponse toModuloResponse(Modulo m) {
        return new ModuloResponse(m.getId(), m.getRuta().getId(), m.getTitulo(), m.getDescripcion(),
                m.getTipo(), m.getOrden(), m.getPuntajeMinimo(),
                m.getIdMoodle(), urlMoodle(m), duracion(m), xp(m), unidad(m), m.getUnidadTitulo(),
                m.getEvaluacion() != null ? m.getEvaluacion().getId() : null);
    }

    public RutaResponse toRutaResponse(Ruta r) {
        return new RutaResponse(r.getId(), r.getNombre(), r.getDescripcion(), r.getVersion(), r.getActiva(),
                r.getModulos().stream().map(this::toModuloResponse).toList(),
                r.getId() != null ? usuarioRepository.countByRutaIdAndActivoTrue(r.getId()) : 0);
    }

    public ProgresoModuloResponse toProgresoResponse(ProgresoModulo p) {
        Modulo m = p.getModulo();
        Ruta ruta = m.getRuta();
        return new ProgresoModuloResponse(p.getId(), m.getId(), m.getTitulo(), m.getOrden(), p.getEstado(),
                p.getCalificacion(), m.getPuntajeMinimo(), p.getFechaInicio(), p.getFechaFin(),
                m.getTipo(), m.getDescripcion(), duracion(m), xp(m), unidad(m), m.getUnidadTitulo(),
                ruta.getId(), ruta.getNombre(), urlMoodle(m),
                m.getEvaluacion() != null ? m.getEvaluacion().getId() : null);
    }

    public SesionResponse toSesionResponse(SesionSeguimiento s) {
        Reserva reserva = s.getId() != null ? reservaRepository.findBySesionId(s.getId()).orElse(null) : null;
        return new SesionResponse(s.getId(),
                s.getManager().getId(), s.getManager().getNombre(),
                s.getEmpleado().getId(), s.getEmpleado().getNombre(),
                s.getFechaHora(), s.getEnlaceTeams(), s.getEstado(), s.getSoporteUrl(),
                s.getAsunto() != null ? s.getAsunto() : SesionSeguimiento.ASUNTO_POR_DEFECTO,
                s.getDescripcion(), s.duracionEfectiva(), s.fechaFin(),
                s.getManager().getCargo(), s.getEmpleado().getCargo(),
                reserva != null ? reserva.getEspacio().getId() : null,
                reserva != null ? reserva.getEspacio().getNombre() : null,
                s.getObservaciones(), s.getFechaVerificacion());
    }

    public EspacioResponse toEspacioResponse(EspacioFisico e) {
        return new EspacioResponse(e.getId(), e.getNombre(), e.getCapacidad(), e.getActivo(), e.getUbicacion());
    }

    public ReservaResponse toReservaResponse(Reserva r) {
        SesionSeguimiento s = r.getSesion();
        return new ReservaResponse(r.getId(), r.getEspacio().getId(), r.getEspacio().getNombre(),
                s != null ? s.getId() : null,
                r.getReservadoPor().getId(), r.getInicio(), r.getFin(),
                r.getEspacio().getCapacidad(), r.getReservadoPor().getNombre(), r.getReservadoPor().getArea(),
                s != null ? (s.getAsunto() != null ? s.getAsunto() : SesionSeguimiento.ASUNTO_POR_DEFECTO) : null,
                s != null ? s.getManager().getNombre() : null);
    }

    public AlertaRezagoResponse toAlertaResponse(AlertaRezago a) {
        boolean activa = a.getFecha() != null
                && a.getFecha().isAfter(LocalDateTime.now().minusDays(DIAS_ALERTA_ACTIVA));
        return new AlertaRezagoResponse(a.getId(), a.getEmpleado().getId(), a.getEmpleado().getNombre(),
                a.getTitulo(), a.getMotivo(), a.getNivel(), a.getFecha(), activa);
    }

    public AuditoriaResponse toAuditoriaResponse(RegistroAuditoria a) {
        return new AuditoriaResponse(a.getId(), a.getUsuario().getId(), a.getUsuario().getNombre(),
                a.getAccion(), a.getEntidad(), a.getEntidadId(), a.getDetalle(), a.getFechaHora());
    }

    public PreferenciasResponse toPreferenciasResponse(PreferenciasUsuario p) {
        return new PreferenciasResponse(p.getRecordatoriosReuniones(), p.getAvisosProgreso(),
                p.getNotificaciones(), p.getIdioma());
    }

    public MensajeResponse toMensajeResponse(Mensaje m) {
        return new MensajeResponse(m.getId(), m.getRemitente().getId(), m.getRemitente().getNombre(),
                m.getDestinatario().getId(), m.getDestinatario().getNombre(), m.getTexto(), m.getFecha());
    }

    public SolicitudSesionResponse toSolicitudResponse(SolicitudSesion s) {
        return new SolicitudSesionResponse(s.getId(), s.getEmpleado().getId(), s.getEmpleado().getNombre(),
                s.getManager().getId(), s.getManager().getNombre(), s.getFechaPropuesta(), s.getComentario(),
                s.getEstado(), s.getFecha());
    }

    private String urlMoodle(Modulo m) {
        if (plantillaUrlMoodle == null || plantillaUrlMoodle.isBlank() || m.getIdMoodle() == null) {
            return null;
        }
        return plantillaUrlMoodle.replace("{id}", m.getIdMoodle());
    }

    private int duracion(Modulo m) {
        return m.getDuracionMinutos() != null ? m.getDuracionMinutos() : Modulo.DURACION_POR_DEFECTO;
    }

    private int xp(Modulo m) {
        return m.getPuntosXp() != null ? m.getPuntosXp() : Modulo.XP_POR_DEFECTO;
    }

    private int unidad(Modulo m) {
        return m.getUnidad() != null ? m.getUnidad() : Modulo.UNIDAD_POR_DEFECTO;
    }
}
