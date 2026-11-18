package com.example.onboarding.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

/** Guarda las fotos de perfil en disco. En producción puede reemplazarse por S3 o Supabase Storage. */
@Component
public class AlmacenamientoLocal implements AlmacenamientoArchivos {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[a-f0-9\\-]{36}\\.(jpg|png)");

    private final Path directorio;

    public AlmacenamientoLocal(@Value("${onboarding.uploads.dir:uploads}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(byte[] contenido, String extension) {
        try {
            Files.createDirectories(directorio);
            String nombre = UUID.randomUUID() + "." + extension;
            Files.write(directorio.resolve(nombre), contenido);
            return nombre;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
    }

    @Override
    public Resource cargar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return null;
        }
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.startsWith(directorio) || !Files.exists(archivo)) {
            return null;
        }
        return new PathResource(archivo);
    }
}
