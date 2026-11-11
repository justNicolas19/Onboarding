package com.example.onboarding.controller;

import com.example.onboarding.dto.response.AuditoriaResponse;
import com.example.onboarding.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    /** Parámetros opcionales: page, size y sort. Por defecto, 20 registros, del más reciente al más antiguo. */
    @GetMapping
    public ResponseEntity<PagedModel<AuditoriaResponse>> consultar(
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(auditoriaService.consultar(pageable)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PagedModel<AuditoriaResponse>> porUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(auditoriaService.consultarPorUsuario(usuarioId, pageable)));
    }
}
