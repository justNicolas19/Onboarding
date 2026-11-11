package com.example.onboarding.controller;

import com.example.onboarding.dto.request.ReservaRequest;
import com.example.onboarding.dto.response.ReservaResponse;
import com.example.onboarding.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'EMPLEADO')")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(usuarioActualId(), request));
    }

    @GetMapping("/mias")
    public ResponseEntity<List<ReservaResponse>> misReservas() {
        return ResponseEntity.ok(reservaService.listarPorUsuario(usuarioActualId()));
    }

    /** Ocupación de un espacio en un rango. Fechas en formato ISO, por ejemplo 2026-11-10T08:00:00. */
    @GetMapping("/espacio/{espacioId}")
    public ResponseEntity<List<ReservaResponse>> porEspacio(
            @PathVariable Long espacioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(reservaService.listarPorEspacio(espacioId, desde, hasta));
    }
}
