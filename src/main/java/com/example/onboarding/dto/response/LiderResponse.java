package com.example.onboarding.dto.response;

/** Líder (usuario con rol MANAGER) visto por un empleado en "Mis líderes" y "Tu líder". */
public record LiderResponse(
        Long id,
        String nombre,
        String correo,
        String cargo,
        String area,
        String presentacion,
        String fotoUrl,
        boolean esMiManager
) {}
