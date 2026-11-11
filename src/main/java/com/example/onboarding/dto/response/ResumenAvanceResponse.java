package com.example.onboarding.dto.response;


/** Fila del panel de seguimiento del manager (RF7). */
public record ResumenAvanceResponse(
        Long empleadoId,
        String empleadoNombre,
        int modulosTotales,
        int modulosCompletados,
        double porcentajeAvance,
        Double promedioCalificacion,
        int sesionesVerificadas,
        boolean conAlertaActiva
) {}