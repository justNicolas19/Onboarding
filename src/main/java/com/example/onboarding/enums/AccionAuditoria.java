package com.example.onboarding.enums;

/** Acciones que quedan registradas en la auditoría (RF11). */
public enum AccionAuditoria {
    INICIO_SESION,
    USUARIO_CREADO,
    USUARIO_MODIFICADO,
    RUTA_ASIGNADA,
    MODULO_ABIERTO,
    MODULO_COMPLETADO,
    MODULO_EN_REFUERZO,
    SESION_AGENDADA,
    SESION_REPROGRAMADA,
    SESION_CANCELADA,
    SESION_REALIZADA,
    SESION_VERIFICADA,
    RESERVA_CREADA,
    ALERTA_GENERADA,
    PLANILLA_EXPORTADA,
    /** Nuevas: pantallas que antes no tenían backend (sección 3 del reporte). */
    SESION_SOLICITADA,
    MENSAJE_ENVIADO,
    PERFIL_ACTUALIZADO
}
