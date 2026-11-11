package com.example.onboarding.controller;



import com.example.onboarding.dto.request.UsuarioRequest;
import com.example.onboarding.dto.response.UsuarioResponse;
import com.example.onboarding.service.UsuarioService;
import com.example.onboarding.util.UbicacionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(usuarioActualId(), request);
        return ResponseEntity.created(UbicacionUtil.de(creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(usuarioActualId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(usuarioActualId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    /** Empleados a cargo del manager autenticado. */
    @GetMapping("/mi-equipo")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UsuarioResponse>> miEquipo() {
        return ResponseEntity.ok(usuarioService.listarEmpleadosDeManager(usuarioActualId()));
    }
}
