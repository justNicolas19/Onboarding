package com.example.onboarding.integration;

import org.springframework.core.io.Resource;

public interface AlmacenamientoArchivos {

    /** Guarda el contenido y devuelve el nombre con el que se puede recuperar. */
    String guardar(byte[] contenido, String extension);

    /** Devuelve el archivo o null si no existe. */
    Resource cargar(String nombre);
}
