package com.example.onboarding.mapper;

import com.example.onboarding.dto.response.*;
import com.example.onboarding.entity.*;
import org.springframework.stereotype.Component;

/** Convierte entities en DTOs de respuesta. Debe usarse dentro de una transacción (relaciones lazy). */
@Component
public class EntityMapper {

    public UsuarioResponse toUsuarioResponse(Usuario u) {
        Usuario m = u.getManager();
        Ruta r = u.getRuta();
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getCargo(),
                u.getFechaIngreso(), u.getActivo(),
                m != null ? m.getId() : null, m != null ? m.getNombre() : null,
                r != null ? r.getId() : null, r != null ? r.getNombre() : null);
    }

    public ModuloResponse toModuloResponse(Modulo m) {
        return new ModuloResponse(m.getId(), m.getRuta().getId(), m.getTitulo(), m.getDescripcion(),
                m.getTipo(), m.getOrden(), m.getPuntajeMinimo());
    }

    public RutaResponse toRutaResponse(Ruta r) {
        return new RutaResponse(r.getId(), r.getNombre(), r.getDescripcion(), r.getVersion(), r.getActiva(),
                r.getModulos().stream().map(this::toModuloResponse).toList());
    }

    public ProgresoModuloResponse toProgresoResponse(ProgresoModulo p) {
        Modulo m = p.getModulo();
        return new ProgresoModuloResponse(p.getId(), m.getId(), m.getTitulo(), m.getOrden(), p.getEstado(),
                p.getCalificacion(), m.getPuntajeMinimo(), p.getFechaInicio(), p.getFechaFin());
    }

    public SesionResponse toSesionResponse(SesionSeguimiento s) {
        return new SesionResponse(s.getId(),
                s.getManager().getId(), s.getManager().getNombre(),
                s.getEmpleado().getId(), s.getEmpleado().getNombre(),
                s.getFechaHora(), s.getEnlaceTeams(), s.getEstado(), s.getSoporteUrl());
    }

    public EspacioResponse toEspacioResponse(EspacioFisico e) {
        return new EspacioResponse(e.getId(), e.getNombre(), e.getCapacidad(), e.getActivo());
    }

    public ReservaResponse toReservaResponse(Reserva r) {
        return new ReservaResponse(r.getId(), r.getEspacio().getId(), r.getEspacio().getNombre(),
                r.getSesion() != null ? r.getSesion().getId() : null,
                r.getReservadoPor().getId(), r.getInicio(), r.getFin());
    }

    public AlertaRezagoResponse toAlertaResponse(AlertaRezago a) {
        return new AlertaRezagoResponse(a.getId(), a.getEmpleado().getId(), a.getEmpleado().getNombre(),
                a.getMotivo(), a.getNivel(), a.getFecha());
    }

    public AuditoriaResponse toAuditoriaResponse(RegistroAuditoria a) {
        return new AuditoriaResponse(a.getId(), a.getUsuario().getId(), a.getUsuario().getNombre(),
                a.getAccion(), a.getEntidad(), a.getEntidadId(), a.getDetalle(), a.getFechaHora());
    }
}
