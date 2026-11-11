package com.example.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Adjunta el soporte que demuestra que la sesión se realizó. */
public record SesionSoporteRequest(
        @NotBlank @Size(max = 500) String soporteUrl
) {}
