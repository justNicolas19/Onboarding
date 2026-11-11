package com.example.onboarding.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/** SOLO PARA DESARROLLO. Se activa con teams.modo=simulado (valor por defecto). */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "teams", name = "modo", havingValue = "simulado", matchIfMissing = true)
public class TeamsClientSimulado implements TeamsClient {

    @Override
    public String crearReunion(String asunto, LocalDateTime inicio, LocalDateTime fin,
                               String correoManager, String correoEmpleado) {
        log.warn("Teams SIMULADO: reunión '{}' el {}", asunto, inicio);
        return "https://teams.microsoft.com/l/meetup-join/simulado-" + UUID.randomUUID();
    }
}
