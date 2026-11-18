package com.example.onboarding.dto.response;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long remitenteId,
        String remitenteNombre,
        Long destinatarioId,
        String destinatarioNombre,
        String texto,
        LocalDateTime fecha
) {}
