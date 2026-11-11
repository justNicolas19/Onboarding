package com.example.onboarding.controller;


import com.example.onboarding.dto.request.RutaRequest;
import com.example.onboarding.dto.response.RutaResponse;
import com.example.onboarding.service.RutaService;
import com.example.onboarding.util.UbicacionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RutaController {

    private final RutaService rutaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RutaResponse> crear(@Valid @RequestBody RutaRequest request) {
        RutaResponse creada = rutaService.crear(request);
        return ResponseEntity.created(UbicacionUtil.de(creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RutaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody RutaRequest request) {
        return ResponseEntity.ok(rutaService.actualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<RutaResponse>> listarActivas() {
        return ResponseEntity.ok(rutaService.listarActivas());
    }
}
