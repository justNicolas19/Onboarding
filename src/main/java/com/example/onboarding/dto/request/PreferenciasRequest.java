package com.example.onboarding.dto.request;

import jakarta.validation.constraints.Size;

/** Todos los campos son opcionales: solo se cambian los que vienen. */
public record PreferenciasRequest(
        Boolean recordatoriosReuniones,
        Boolean avisosProgreso,
        Boolean notificaciones,
        @Size(min = 2, max = 10) String idioma
) {}
