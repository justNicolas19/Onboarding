package com.example.onboarding.controller;

import com.example.onboarding.dto.request.PreferenciasRequest;
import com.example.onboarding.dto.request.UsuarioRequest;
import com.example.onboarding.dto.response.PreferenciasResponse;
import com.example.onboarding.dto.response.UsuarioResponse;
import com.example.onboarding.service.PerfilService;
import com.example.onboarding.service.UsuarioService;
import com.example.onboarding.util.UbicacionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;

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

    /* ---------- Mi perfil: cualquier rol (hueco 6 y secciones 3.4 y 3.5 del reporte) ---------- */

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> yo() {
        return ResponseEntity.ok(perfilService.obtenerMiPerfil(usuarioActualId()));
    }

    @GetMapping("/me/preferencias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PreferenciasResponse> preferencias() {
        return ResponseEntity.ok(perfilService.obtenerPreferencias(usuarioActualId()));
    }

    @PutMapping("/me/preferencias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PreferenciasResponse> actualizarPreferencias(@Valid @RequestBody PreferenciasRequest request) {
        return ResponseEntity.ok(perfilService.actualizarPreferencias(usuarioActualId(), request));
    }

    /** Multipart con el campo "archivo" (JPG o PNG, máximo 5 MB). */
    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponse> subirFoto(@RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(perfilService.subirFoto(usuarioActualId(), archivo));
    }
}
