package com.example.onboarding.exception;

public class IntegracionException extends RuntimeException {
    public IntegracionException(String mensaje) {
        super(mensaje);
    }

    public IntegracionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
