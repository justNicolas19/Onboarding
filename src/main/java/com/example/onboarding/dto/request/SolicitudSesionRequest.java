package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** El empleado pide a su manager una sesión. Ambos campos son opcionales. */
public record SolicitudSesionRequest(
        LocalDateTime fechaPropuesta,
        @Size(max = 300) String comentario
) {}
