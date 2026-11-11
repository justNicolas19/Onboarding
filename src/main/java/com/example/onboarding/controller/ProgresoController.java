package com.example.onboarding.controller;

import com.example.onboarding.dto.request.ReportarAvanceRequest;
import com.example.onboarding.dto.response.ProgresoModuloResponse;
import com.example.onboarding.dto.response.ResumenAvanceResponse;
import com.example.onboarding.service.ProgresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/progreso")
@RequiredArgsConstructor
public class ProgresoController {

    private final ProgresoService progresoService;

    /** Ruta del empleado autenticado, con el estado de cada módulo. */
    @GetMapping("/mi-ruta")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<ProgresoModuloResponse>> miRuta() {
        return ResponseEntity.ok(progresoService.listarRuta(usuarioActualId()));
    }

    @PostMapping("/modulos/{moduloId}/abrir")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<ProgresoModuloResponse> abrirModulo(@PathVariable Long moduloId) {
        return ResponseEntity.ok(progresoService.abrirModulo(usuarioActualId(), moduloId));
    }

    @PostMapping("/avance")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<ProgresoModuloResponse> reportarAvance(@Valid @RequestBody ReportarAvanceRequest request) {
        return ResponseEntity.ok(progresoService.reportarAvance(usuarioActualId(), request));
    }

    /** Panel de seguimiento del manager. */
    @GetMapping("/equipo")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ResumenAvanceResponse>> resumenEquipo() {
        return ResponseEntity.ok(progresoService.resumenEquipo(usuarioActualId()));
    }

    @GetMapping("/empleados/{empleadoId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ProgresoModuloResponse>> rutaDeEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(progresoService.listarRutaDeEmpleado(usuarioActualId(), empleadoId));
    }
}