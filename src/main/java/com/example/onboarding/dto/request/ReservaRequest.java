package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservaRequest(
        @NotNull Long espacioId,
        Long sesionId,
        @NotNull @Future LocalDateTime inicio,
        @NotNull @Future LocalDateTime fin
) {}