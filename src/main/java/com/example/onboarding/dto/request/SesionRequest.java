package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Sirve para agendar y para reprogramar. El manager sale del usuario autenticado. */
public record SesionRequest(
        @NotNull Long empleadoId,
        @NotNull @Future LocalDateTime fechaHora,
        Long espacioId
) {}
