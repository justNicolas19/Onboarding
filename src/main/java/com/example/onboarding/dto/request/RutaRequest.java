package com.example.onboarding.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RutaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        Boolean activa
) {}
