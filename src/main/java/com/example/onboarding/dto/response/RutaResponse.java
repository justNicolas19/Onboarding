package com.example.onboarding.dto.response;

import java.util.List;

public record RutaResponse(
        Long id,
        String nombre,
        String descripcion,
        Integer version,
        Boolean activa,
        List<ModuloResponse> modulos
) {}
