package com.example.onboarding.service;

import com.example.onboarding.dto.request.ModuloRequest;
import com.example.onboarding.dto.response.ModuloResponse;

import java.util.List;

public interface ModuloService {

    ModuloResponse crear(ModuloRequest request);

    ModuloResponse actualizar(Long id, ModuloRequest request);

    void eliminar(Long id);

    List<ModuloResponse> listarPorRuta(Long rutaId);
}
