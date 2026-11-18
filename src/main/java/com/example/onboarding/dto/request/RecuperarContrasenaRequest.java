package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** "¿Problemas para acceder?" en la pantalla de inicio de sesión. */
public record RecuperarContrasenaRequest(
        @NotBlank @Email String correo
) {}
