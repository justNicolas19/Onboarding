package com.example.onboarding.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/** SOLO PARA DESARROLLO: reemplazar por la integración real con Microsoft Graph. */
@Slf4j
@Component
public class TeamsClientSimulado implements TeamsClient {

    @Override
    public String crearReunion(String asunto, LocalDateTime inicio, LocalDateTime fin,
                               String correoManager, String correoEmpleado) {
        log.warn("Teams SIMULADO: reunión '{}' el {}", asunto, inicio);
        return "https://teams.microsoft.com/l/meetup-join/simulado-" + UUID.randomUUID();
    }
}