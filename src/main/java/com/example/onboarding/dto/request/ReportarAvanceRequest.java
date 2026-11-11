package com.example.onboarding.dto.request;


import jakarta.validation.constraints.NotNull;

/**
 * El empleado avisa que terminó un módulo. La calificación no viaja desde la app:
 * el servidor la consulta directamente en Moodle.
 */
public record ReportarAvanceRequest(
        @NotNull Long moduloId
) {}
