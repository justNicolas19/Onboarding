package com.example.onboarding.dto.response;

import com.example.onboarding.enums.NivelAlerta;

import java.time.LocalDateTime;

public record AlertaRezagoResponse(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        String motivo,
        NivelAlerta nivel,
        LocalDateTime fecha
) {}
