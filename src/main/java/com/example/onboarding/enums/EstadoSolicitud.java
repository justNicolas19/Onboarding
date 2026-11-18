package com.example.onboarding.enums;

/** Estado de una solicitud de sesión enviada por un empleado a su manager. */
public enum EstadoSolicitud {
    PENDIENTE,
    /** El manager ya agendó una sesión con el empleado después de la solicitud. */
    ATENDIDA
}
