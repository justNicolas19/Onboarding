package com.example.onboarding.controller;

import com.example.onboarding.service.PlanillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.example.onboarding.util.SecurityUtils.usuarioActualId;

@RestController
@RequestMapping("/api/planillas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMINISTRADOR')")
public class PlanillaController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final PlanillaService planillaService;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) Long empleadoId) {
        byte[] archivo = planillaService.exportarExcel(usuarioActualId(), empleadoId);
        String nombre = "planilla-cumplimiento-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(XLSX)
                .body(archivo);
    }
}
