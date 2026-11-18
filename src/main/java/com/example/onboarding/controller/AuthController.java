package com.example.onboarding.controller;

import com.example.onboarding.dto.request.LoginRequest;
import com.example.onboarding.dto.request.RecuperarContrasenaRequest;
import com.example.onboarding.dto.response.AuthResponse;
import com.example.onboarding.dto.response.MensajeSimpleResponse;
import com.example.onboarding.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos de acceso (sección 3.1 del reporte). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }

    /** Responde siempre 202 para no revelar qué correos existen. */
    @PostMapping("/recuperar")
    public ResponseEntity<MensajeSimpleResponse> recuperar(@Valid @RequestBody RecuperarContrasenaRequest request) {
        authService.solicitarRecuperacion(request.correo());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MensajeSimpleResponse(
                "Si el correo está registrado, te enviaremos las instrucciones para acceder."));
    }
}
