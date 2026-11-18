package com.example.onboarding.service;

import com.example.onboarding.dto.request.PreferenciasRequest;
import com.example.onboarding.dto.response.PreferenciasResponse;
import com.example.onboarding.dto.response.UsuarioResponse;
import org.springframework.web.multipart.MultipartFile;

/** "Mi perfil" y "Subir foto" para cualquier rol (secciones 3.4 y 3.5 del reporte). */
public interface PerfilService {

    UsuarioResponse obtenerMiPerfil(Long usuarioId);

    PreferenciasResponse obtenerPreferencias(Long usuarioId);

    PreferenciasResponse actualizarPreferencias(Long usuarioId, PreferenciasRequest request);

    UsuarioResponse subirFoto(Long usuarioId, MultipartFile archivo);
}
