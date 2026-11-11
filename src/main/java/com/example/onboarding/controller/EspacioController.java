package com.example.onboarding.controller;

import com.example.onboarding.dto.request.EspacioRequest;
import com.example.onboarding.dto.response.EspacioResponse;
import com.example.onboarding.service.EspacioService;
import com.example.onboarding.util.UbicacionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EspacioController {

    private final EspacioService espacioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EspacioResponse> crear(@Valid @RequestBody EspacioRequest request) {
        EspacioResponse creado = espacioService.crear(request);
        return ResponseEntity.created(UbicacionUtil.de(creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EspacioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.ok(espacioService.actualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(espacioService.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<EspacioResponse>> listarActivos() {
        return ResponseEntity.ok(espacioService.listarActivos());
    }
}
