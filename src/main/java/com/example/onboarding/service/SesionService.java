package com.example.onboarding.service;

import com.example.onboarding.dto.request.SesionRequest;
import com.example.onboarding.dto.request.SesionSoporteRequest;
import com.example.onboarding.dto.response.SesionResponse;

import java.util.List;

public interface SesionService {

    SesionResponse agendar(Long managerId, SesionRequest request);

    SesionResponse reprogramar(Long managerId, Long sesionId, SesionRequest request);

    SesionResponse cancelar(Long managerId, Long sesionId);

    SesionResponse marcarRealizada(Long managerId, Long sesionId);

    /** Adjunta el soporte y deja la sesión VERIFICADA. */
    SesionResponse adjuntarSoporte(Long managerId, Long sesionId, SesionSoporteRequest request);

    SesionResponse obtener(Long sesionId);

    List<SesionResponse> listarPorManager(Long managerId);

    List<SesionResponse> listarPorEmpleado(Long empleadoId);
}
