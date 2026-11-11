package com.example.onboarding.dto.response;

import com.example.onboarding.enums.EstadoProgreso;

import java.time.LocalDateTime;

/** Estado de un módulo para el empleado. Incluye lo necesario para pintar el candado en la app. */
public record ProgresoModuloResponse(
        Long id,
        Long moduloId,
        String moduloTitulo,
        Integer orden,
        EstadoProgreso estado,
        Double calificacion,
        Integer puntajeMinimo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {}
