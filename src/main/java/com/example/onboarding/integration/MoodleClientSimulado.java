package com.example.onboarding.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** SOLO PARA DESARROLLO: reemplazar por la integración real con los web services de Moodle. */
@Slf4j
@Component
public class MoodleClientSimulado implements MoodleClient {

    @Override
    public Double consultarCalificacion(String idUsuarioMoodle, String idActividadMoodle) {
        log.warn("Moodle SIMULADO: devolviendo 100 para usuario {} en actividad {}",
                idUsuarioMoodle, idActividadMoodle);
        return 100.0;
    }
}
