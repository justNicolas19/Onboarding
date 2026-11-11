package com.example.onboarding.service;

import com.example.onboarding.dto.response.AuditoriaResponse;
import com.example.onboarding.enums.AccionAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditoriaService {

    void registrar(Long usuarioId, AccionAuditoria accion, String entidad, Long entidadId, String detalle);

    Page<AuditoriaResponse> consultar(Pageable pageable);

    Page<AuditoriaResponse> consultarPorUsuario(Long usuarioId, Pageable pageable);
}
