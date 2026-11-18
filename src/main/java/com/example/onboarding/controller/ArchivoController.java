package com.example.onboarding.controller;

import com.example.onboarding.integration.AlmacenamientoArchivos;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/** Sirve las fotos de perfil. Es público: los nombres son UUID aleatorios. */
@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoController {

    private final AlmacenamientoArchivos almacenamiento;

    @GetMapping("/fotos/{nombre}")
    public ResponseEntity<Resource> foto(@PathVariable String nombre) {
        Resource recurso = almacenamiento.cargar(nombre);
        if (recurso == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo = nombre.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(tipo)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(recurso);
    }
}
