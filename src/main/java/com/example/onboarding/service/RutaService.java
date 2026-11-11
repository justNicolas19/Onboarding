package com.example.onboarding.service;

import com.example.onboarding.dto.request.RutaRequest;
import com.example.onboarding.dto.response.RutaResponse;

import java.util.List;

public interface RutaService {

    RutaResponse crear(RutaRequest request);

    RutaResponse actualizar(Long id, RutaRequest request);

    RutaResponse obtener(Long id);

    List<RutaResponse> listarActivas();
}
