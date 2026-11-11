package com.example.onboarding.dto.response;

import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        Long espacioId,
        String espacioNombre,
        Long sesionId,
        Long reservadoPorId,
        LocalDateTime inicio,
        LocalDateTime fin
) {}
