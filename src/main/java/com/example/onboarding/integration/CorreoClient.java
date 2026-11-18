package com.example.onboarding.integration;

public interface CorreoClient {

    /** Envía las instrucciones para recuperar el acceso. */
    void enviarRecuperacion(String correo, String nombre);
}
