package com.example.onboarding.dto.response;


public record EspacioResponse(
        Long id,
        String nombre,
        Integer capacidad,
        Boolean activo
) {}
