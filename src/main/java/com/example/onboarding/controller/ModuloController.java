package com.example.onboarding.controller;

import com.example.onboarding.dto.request.ModuloRequest;
import com.example.onboarding.dto.response.ModuloResponse;
import com.example.onboarding.service.ModuloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ModuloController {

    private final ModuloService moduloService;

    @PostMapping("/modulos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ModuloResponse> crear(@Valid @RequestBody ModuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduloService.crear(request));
    }

    @PutMapping("/modulos/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ModuloResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ModuloRequest request) {
        return ResponseEntity.ok(moduloService.actualizar(id, request));
    }

    @DeleteMapping("/modulos/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        moduloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rutas/{rutaId}/modulos")
    public ResponseEntity<List<ModuloResponse>> listarPorRuta(@PathVariable Long rutaId) {
        return ResponseEntity.ok(moduloService.listarPorRuta(rutaId));
    }
}
