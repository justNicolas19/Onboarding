package com.example.onboarding.dto.response;

import com.example.onboarding.enums.EstadoProgreso;
import com.example.onboarding.enums.TipoModulo;

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
        LocalDateTime fechaFin,
        /* Nuevos (sección 4 del reporte): ya estaban en Modulo, ahora viajan a la app. */
        TipoModulo tipo,
        String descripcion,
        Integer duracionMinutos,
        Integer puntosXp,
        Integer unidad,
        String unidadTitulo,
        Long rutaId,
        String rutaNombre,
        String urlMoodle,
        Long evaluacionModuloId
) {}
