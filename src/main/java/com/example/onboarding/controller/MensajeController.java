package com.example.onboarding.controller;

import com.example.onboarding.dto.request.MensajeRequest;
import com.example.onboarding.dto.response.MensajeResponse;
import com.example.onboarding.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MensajeController {

    private final MensajeService mensajeService;

    @PostMapping
    public ResponseEntity<MensajeResponse> enviar(@Valid @RequestBody MensajeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mensajeService.enviar(usuarioActualId(), request));
    }

    @GetMapping("/recibidos")
    public ResponseEntity<List<MensajeResponse>> recibidos() {
        return ResponseEntity.ok(mensajeService.recibidos(usuarioActualId()));
    }
}
