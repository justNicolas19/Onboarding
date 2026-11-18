package com.example.onboarding.dto.response;

import com.example.onboarding.enums.TipoModulo;

public record ModuloResponse(
        Long id,
        Long rutaId,
        String titulo,
        String descripcion,
        TipoModulo tipo,
        Integer orden,
        Integer puntajeMinimo,
        /* Nuevos (hueco 4 y sección 4 del reporte). */
        String idMoodle,
        String urlMoodle,
        Integer duracionMinutos,
        Integer puntosXp,
        Integer unidad,
        String unidadTitulo,
        Long evaluacionId
) {}
