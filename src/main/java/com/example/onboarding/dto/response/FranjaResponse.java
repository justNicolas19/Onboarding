package com.example.onboarding.dto.response;

import java.time.LocalDateTime;

/** Franja del "Calendario de ocupación" de un espacio. */
public record FranjaResponse(
        LocalDateTime inicio,
        LocalDateTime fin,
        boolean disponible,
        /** Quién ocupa la franja; null si está libre. */
        String reservadoPorNombre,
        /** Asunto de la sesión vinculada a la reserva que ocupa la franja, si existe. */
        String sesionAsunto
) {}
