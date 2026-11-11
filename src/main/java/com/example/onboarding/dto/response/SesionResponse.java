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
        String soporteUrl
) {}
