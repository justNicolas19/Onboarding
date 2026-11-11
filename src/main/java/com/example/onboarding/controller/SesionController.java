package com.example.onboarding.controller;

import com.example.onboarding.dto.request.SesionRequest;
import com.example.onboarding.dto.request.SesionSoporteRequest;
import com.example.onboarding.dto.response.SesionResponse;
import com.example.onboarding.exception.AccesoDenegadoException;
import com.example.onboarding.service.SesionService;
import com.example.onboarding.util.UbicacionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final SesionService sesionService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SesionResponse> agendar(@Valid @RequestBody SesionRequest request) {
        SesionResponse creada = sesionService.agendar(usuarioActualId(), request);
        return ResponseEntity.created(UbicacionUtil.de(creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SesionResponse> reprogramar(@PathVariable Long id,
                                                      @Valid @RequestBody SesionRequest request) {
        return ResponseEntity.ok(sesionService.reprogramar(usuarioActualId(), id, request));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SesionResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(sesionService.cancelar(usuarioActualId(), id));
    }

    @PostMapping("/{id}/realizar")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SesionResponse> marcarRealizada(@PathVariable Long id) {
        return ResponseEntity.ok(sesionService.marcarRealizada(usuarioActualId(), id));
    }

    @PostMapping("/{id}/soporte")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SesionResponse> adjuntarSoporte(@PathVariable Long id,
                                                          @Valid @RequestBody SesionSoporteRequest request) {
        return ResponseEntity.ok(sesionService.adjuntarSoporte(usuarioActualId(), id, request));
    }

    /** Solo el manager o el empleado de la sesión pueden verla. */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SesionResponse> obtener(@PathVariable Long id) {
        SesionResponse sesion = sesionService.obtener(id);
        Long actual = usuarioActualId();
        if (!actual.equals(sesion.managerId()) && !actual.equals(sesion.empleadoId())) {
            throw new AccesoDenegadoException("No participas en esta sesión");
        }
        return ResponseEntity.ok(sesion);
    }

    @GetMapping("/mi-agenda")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<SesionResponse>> miAgenda() {
        return ResponseEntity.ok(sesionService.listarPorManager(usuarioActualId()));
    }

    @GetMapping("/mis-sesiones")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<SesionResponse>> misSesiones() {
        return ResponseEntity.ok(sesionService.listarPorEmpleado(usuarioActualId()));
    }
}
