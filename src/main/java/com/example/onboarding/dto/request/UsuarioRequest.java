package com.example.onboarding.dto.request;

import com.example.onboarding.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 160) String correo,
        /** Obligatoria al crear; opcional al actualizar (si viene vacía se conserva la actual). */
        @Size(min = 8, max = 72) String contrasena,
        @NotNull Rol rol,
        @Size(max = 100) String cargo,
        LocalDate fechaIngreso,
        @Size(max = 60) String idMoodle,
        Long managerId,
        Long rutaId
) {}