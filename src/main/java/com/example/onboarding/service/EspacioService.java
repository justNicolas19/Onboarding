package com.example.onboarding.service;

import com.example.onboarding.dto.request.EspacioRequest;
import com.example.onboarding.dto.response.EspacioResponse;

import java.util.List;

public interface EspacioService {

    EspacioResponse crear(EspacioRequest request);

    EspacioResponse actualizar(Long id, EspacioRequest request);

    EspacioResponse obtener(Long id);

    List<EspacioResponse> listarActivos();
}
