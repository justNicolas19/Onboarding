package com.example.onboarding.dto.request;

import com.example.onboarding.enums.TipoModulo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModuloRequest(
        @NotNull Long rutaId,
        @NotBlank @Size(max = 150) String titulo,
        @Size(max = 500) String descripcion,
        @NotNull TipoModulo tipo,
        @NotNull @Min(1) Integer orden,
        @NotBlank @Size(max = 60) String idMoodle,
        @Min(0) @Max(100) Integer puntajeMinimo,
        /* Nuevos y opcionales (sección 4 del reporte). */
        @Min(1) @Max(600) Integer duracionMinutos,
        @Min(0) @Max(10000) Integer puntosXp,
        @Min(1) Integer unidad,
        @Size(max = 120) String unidadTitulo,
        /** Id del módulo de evaluación asociado; debe ser de la misma ruta. */
        Long evaluacionId
) {}
