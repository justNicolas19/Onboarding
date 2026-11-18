package com.example.onboarding.service;

import com.example.onboarding.dto.request.UsuarioRequest;
import com.example.onboarding.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse crear(Long actorId, UsuarioRequest request);

    /** Reemplaza los datos del usuario. Si no viene contraseña o idMoodle se conservan los actuales. */
    UsuarioResponse actualizar(Long actorId, Long id, UsuarioRequest request);

    void desactivar(Long actorId, Long id);

    UsuarioResponse obtener(Long id);

    List<UsuarioResponse> listar();

    List<UsuarioResponse> listarEmpleadosDeManager(Long managerId);
}
