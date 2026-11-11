package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.response.AlertaRezagoResponse;
import com.example.onboarding.entity.AlertaRezago;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.NivelAlerta;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.AlertaRezagoRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AlertaRezagoService;
import com.example.onboarding.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaRezagoServiceImpl implements AlertaRezagoService {


    private static final int MAX_MOTIVO = 300;

    private final AlertaRezagoRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public AlertaRezagoResponse generar(Long empleadoId, String motivo, NivelAlerta nivel) {
        Usuario empleado = usuarioRepository.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + empleadoId));
        if (motivo.length() > MAX_MOTIVO) {
            motivo = motivo.substring(0, MAX_MOTIVO);
        }
        AlertaRezago alerta = alertaRepository.save(AlertaRezago.builder()
                .empleado(empleado)
                .motivo(motivo)
                .nivel(nivel)
                .build());

        // Acción del sistema: se registra a nombre del empleado afectado.
        auditoriaService.registrar(empleadoId, AccionAuditoria.ALERTA_GENERADA, "AlertaRezago", alerta.getId(),
                "Generada por el sistema (" + nivel + "): " + motivo);
        return mapper.toAlertaResponse(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaRezagoResponse> listarPorEmpleado(Long empleadoId) {
        return alertaRepository.findByEmpleadoIdOrderByFechaDesc(empleadoId).stream()
                .map(mapper::toAlertaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaRezagoResponse> listarPorManager(Long managerId) {
        return alertaRepository.findByEmpleadoManagerIdOrderByFechaDesc(managerId).stream()
                .map(mapper::toAlertaResponse).toList();
    }
}
