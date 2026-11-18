package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Sirve para agendar y para reprogramar. El manager sale del usuario autenticado. */
public record SesionRequest(
        @NotNull Long empleadoId,
        @NotNull @Future LocalDateTime fechaHora,
        Long espacioId,
        /* Nuevos y opcionales (sección 4 del reporte). */
        @Size(max = 150) String asunto,
        @Size(max = 500) String descripcion,
        /** Duración en minutos; si no viene se usa 60 (el valor fijo anterior). */
        @Min(15) @Max(240) Integer duracionMinutos
) {}
