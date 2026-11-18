package com.example.onboarding.dto.response;

import com.example.onboarding.enums.NivelAlerta;

import java.time.LocalDateTime;

public record AlertaRezagoResponse(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        /** Nuevo: título corto; puede venir null en alertas antiguas. */
        String titulo,
        String motivo,
        NivelAlerta nivel,
        LocalDateTime fecha,
        /** Nuevo: true si la alerta es de los últimos 7 días. */
        boolean activa
) {}
