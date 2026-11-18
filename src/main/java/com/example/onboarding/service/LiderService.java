package com.example.onboarding.service;

import com.example.onboarding.dto.response.LiderResponse;

import java.util.List;

public interface LiderService {

    /** Managers activos; el manager del empleado aparece primero y marcado con esMiManager. */
    List<LiderResponse> listarParaEmpleado(Long empleadoId);
}
