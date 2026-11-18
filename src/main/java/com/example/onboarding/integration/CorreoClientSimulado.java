package com.example.onboarding.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** SOLO PARA DESARROLLO: reemplazar por el servicio de correo corporativo (SMTP o Microsoft Graph). */
@Slf4j
@Component
public class CorreoClientSimulado implements CorreoClient {

    @Override
    public void enviarRecuperacion(String correo, String nombre) {
        log.warn("Correo SIMULADO: instrucciones de recuperación para {} <{}>", nombre, correo);
    }
}
