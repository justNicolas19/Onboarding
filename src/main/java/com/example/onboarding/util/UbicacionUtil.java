package com.example.onboarding.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class UbicacionUtil {

    private UbicacionUtil() {
    }

    /** URI del recurso recién creado: la ruta actual más "/{id}". */
    public static URI de(Object id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
