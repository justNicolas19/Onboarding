package com.example.onboarding.enums;

/**
 * Ciclo de vida de un módulo para un empleado.
 * BLOQUEADO -> DISPONIBLE -> EN_CURSO -> COMPLETADO (o EN_REFUERZO y vuelta a EN_CURSO).
 */
public enum EstadoProgreso {
    BLOQUEADO,
    DISPONIBLE,
    EN_CURSO,
    EN_REFUERZO,
    COMPLETADO
}
