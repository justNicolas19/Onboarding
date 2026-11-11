package com.example.onboarding.dto.response;


import com.example.onboarding.enums.TipoModulo;

public record ModuloResponse(
        Long id,
        Long rutaId,
        String titulo,
        String descripcion,
        TipoModulo tipo,
        Integer orden,
        Integer puntajeMinimo
) {}
