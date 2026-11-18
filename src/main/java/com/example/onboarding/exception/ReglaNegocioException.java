package com.example.onboarding.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReglaNegocioException extends RuntimeException {

    /** Código estable opcional que la app puede leer en ErrorResponse.codigo. */
    private final String codigo;

    public ReglaNegocioException(String mensaje) {
        this(null, mensaje);
    }

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
