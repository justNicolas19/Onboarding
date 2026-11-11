package com.example.onboarding.service;

import com.example.onboarding.dto.response.AlertaRezagoResponse;
import com.example.onboarding.enums.NivelAlerta;

import java.util.List;

public interface AlertaRezagoService {

    /** Lo invoca el motor de IA cuando detecta rezago. */
    AlertaRezagoResponse generar(Long empleadoId, String motivo, NivelAlerta nivel);

    List<AlertaRezagoResponse> listarPorEmpleado(Long empleadoId);

    List<AlertaRezagoResponse> listarPorManager(Long managerId);
}