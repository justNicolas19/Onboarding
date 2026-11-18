package com.example.onboarding.controller;

import com.example.onboarding.dto.response.LiderResponse;
import com.example.onboarding.service.LiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

/** "Mis líderes" y "Tu líder" (sección 3.3 del reporte). */
@RestController
@RequestMapping("/api/lideres")
@RequiredArgsConstructor
public class LiderController {

    private final LiderService liderService;

    @GetMapping
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<LiderResponse>> listar() {
        return ResponseEntity.ok(liderService.listarParaEmpleado(usuarioActualId()));
    }
}
