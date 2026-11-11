package com.example.onboarding.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        List<String> detalles
) {}
