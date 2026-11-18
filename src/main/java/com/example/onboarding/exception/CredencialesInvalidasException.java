package com.example.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Correo o contraseña incorrectos, o cuenta desactivada. Responde 401. */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
