package com.example.onboarding.exception;


public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
