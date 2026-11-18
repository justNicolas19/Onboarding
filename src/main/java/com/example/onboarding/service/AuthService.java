package com.example.onboarding.service;

import com.example.onboarding.dto.request.LoginRequest;
import com.example.onboarding.dto.response.AuthResponse;

public interface AuthService {

    /** Valida correo y contraseña, rechaza cuentas desactivadas y registra INICIO_SESION. */
    AuthResponse iniciarSesion(LoginRequest request);

    /** Envía instrucciones si el correo existe. No revela si existe o no. */
    void solicitarRecuperacion(String correo);
}
