package com.example.onboarding.service;

import com.example.onboarding.dto.request.RutaRequest;
import com.example.onboarding.dto.response.RutaResponse;

import java.util.List;

public interface RutaService {

    RutaResponse crear(RutaRequest request);

    RutaResponse actualizar(Long id, RutaRequest request);

    RutaResponse obtener(Long id);

    List<RutaResponse> listarActivas();

    /** Todas las rutas, incluidas las de borrador (solo administrador; hueco 5 del reporte). */
    List<RutaResponse> listarTodas();
}
