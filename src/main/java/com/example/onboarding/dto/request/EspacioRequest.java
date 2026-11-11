package com.example.onboarding.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EspacioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @Min(1) Integer capacidad,
        Boolean activo
) {}
