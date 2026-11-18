package com.example.onboarding.dto.response;

public record PreferenciasResponse(
        boolean recordatoriosReuniones,
        boolean avisosProgreso,
        boolean notificaciones,
        String idioma
) {}
