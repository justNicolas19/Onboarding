package com.example.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensajeRequest(
        @NotNull Long destinatarioId,
        @NotBlank @Size(max = 1000) String texto
) {}
