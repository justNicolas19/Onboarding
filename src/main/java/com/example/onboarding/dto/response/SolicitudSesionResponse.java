package com.example.onboarding.dto.response;

import com.example.onboarding.enums.EstadoSolicitud;

import java.time.LocalDateTime;

public record SolicitudSesionResponse(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        Long managerId,
        String managerNombre,
        LocalDateTime fechaPropuesta,
        String comentario,
        EstadoSolicitud estado,
        LocalDateTime fecha
) {}
