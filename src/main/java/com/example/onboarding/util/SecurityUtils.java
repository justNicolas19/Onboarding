package com.example.onboarding.util;

import com.example.onboarding.exception.AccesoDenegadoException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Id del usuario autenticado. El filtro JWT debe dejarlo como nombre del principal. */
    public static Long usuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccesoDenegadoException("No hay una sesión autenticada");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new AccesoDenegadoException("La sesión no es válida");
        }
    }
}