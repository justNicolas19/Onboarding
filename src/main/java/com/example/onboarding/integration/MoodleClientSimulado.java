package com.example.onboarding.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** SOLO PARA DESARROLLO. Se activa con moodle.modo=simulado (valor por defecto). */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "moodle", name = "modo", havingValue = "simulado", matchIfMissing = true)
public class MoodleClientSimulado implements MoodleClient {

    @Override
    public Double consultarCalificacion(String idUsuarioMoodle, String idActividadMoodle) {
        log.warn("Moodle SIMULADO: devolviendo 100 para usuario {} en actividad {}",
                idUsuarioMoodle, idActividadMoodle);
        return 100.0;
    }
}
