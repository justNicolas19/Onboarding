package com.example.onboarding.integration;

import java.time.LocalDateTime;

public interface TeamsClient {

    /** Crea la reunión en Teams y devuelve el enlace para unirse. */
    String crearReunion(String asunto, LocalDateTime inicio, LocalDateTime fin,
                        String correoManager, String correoEmpleado);
}