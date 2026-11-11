package com.example.onboarding.integration;


public interface MoodleClient {

    /** Devuelve la calificación (0 a 100) del usuario en la actividad, o null si aún no existe. */
    Double consultarCalificacion(String idUsuarioMoodle, String idActividadMoodle);

}