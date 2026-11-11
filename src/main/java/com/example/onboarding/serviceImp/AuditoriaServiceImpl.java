package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.response.AuditoriaResponse;
import com.example.onboarding.entity.RegistroAuditoria;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.RegistroAuditoriaRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {
    private static final int MAX_DETALLE = 1000;

    private final RegistroAuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public void registrar(Long usuarioId, AccionAuditoria accion, String entidad, Long entidadId, String detalle) {
        if (detalle != null && detalle.length() > MAX_DETALLE) {
            detalle = detalle.substring(0, MAX_DETALLE);
        }
        RegistroAuditoria registro = RegistroAuditoria.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .detalle(detalle)
                .build();
        auditoriaRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> consultar(Pageable pageable) {
        return auditoriaRepository.findAll(pageable).map(mapper::toAuditoriaResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> consultarPorUsuario(Long usuarioId, Pageable pageable) {
        return auditoriaRepository.findByUsuarioId(usuarioId, pageable).map(mapper::toAuditoriaResponse);
    }
}
