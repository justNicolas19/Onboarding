package com.example.onboarding.enums;

/** Ciclo de vida de una sesión de seguimiento. */
public enum EstadoSesion {
    PROGRAMADA,
    CANCELADA,
    REALIZADA,
    /** La sesión tiene soporte adjunto y cuenta para el mínimo exigido en la auditoría. */
    VERIFICADA
}