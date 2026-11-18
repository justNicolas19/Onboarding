package com.example.onboarding.dto.response;

import com.example.onboarding.enums.EstadoSesion;

import java.time.LocalDateTime;

public record SesionResponse(
        Long id,
        Long managerId,
        String managerNombre,
        Long empleadoId,
        String empleadoNombre,
        LocalDateTime fechaHora,
        String enlaceTeams,
        EstadoSesion estado,
        String soporteUrl,
        /* Nuevos (sección 4 del reporte). */
        String asunto,
        String descripcion,
        int duracionMinutos,
        LocalDateTime fechaFin,
        String managerCargo,
        String empleadoCargo,
        Long espacioId,
        String espacioNombre,
        String observaciones,
        LocalDateTime fechaVerificacion
) {}
