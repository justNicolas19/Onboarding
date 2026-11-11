package com.example.onboarding.dto.response;

import com.example.onboarding.enums.Rol;

import java.time.LocalDateTime;

/** Respuesta del login. La app guarda el token y lo envía en cada petición. */
public record AuthResponse(
        String token,
        LocalDateTime expiraEn,
        Long usuarioId,
        String nombre,
        Rol rol
) {}
