package com.example.onboarding.controller;



import com.example.onboarding.dto.response.AlertaRezagoResponse;
import com.example.onboarding.service.AlertaRezagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaRezagoController {

    private final AlertaRezagoService alertaService;

    @GetMapping("/mi-equipo")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AlertaRezagoResponse>> delEquipo() {
        return ResponseEntity.ok(alertaService.listarPorManager(usuarioActualId()));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<AlertaRezagoResponse>> mias() {
        return ResponseEntity.ok(alertaService.listarPorEmpleado(usuarioActualId()));
    }
}
