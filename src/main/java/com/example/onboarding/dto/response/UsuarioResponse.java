package com.example.onboarding.dto.response;

import com.example.onboarding.enums.Rol;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Rol rol,
        String cargo,
        LocalDate fechaIngreso,
        Boolean activo,
        Long managerId,
        String managerNombre,
        Long rutaId,
        String rutaNombre
) {}
