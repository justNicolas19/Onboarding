package com.example.onboarding.dto.response;

import com.example.onboarding.enums.AccionAuditoria;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        AccionAuditoria accion,
        String entidad,
        Long entidadId,
        String detalle,
        LocalDateTime fechaHora
) {}
