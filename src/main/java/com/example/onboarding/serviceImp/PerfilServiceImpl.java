package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.PreferenciasRequest;
import com.example.onboarding.dto.response.PreferenciasResponse;
import com.example.onboarding.dto.response.UsuarioResponse;
import com.example.onboarding.entity.PreferenciasUsuario;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.integration.AlmacenamientoArchivos;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.PreferenciasUsuarioRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    /** "JPG o PNG · máximo 5 MB" (pantalla Subir foto). */
    private static final long MAX_FOTO_BYTES = 5L * 1024 * 1024;
    public static final String RUTA_FOTOS = "/api/archivos/fotos/";

    private final UsuarioRepository usuarioRepository;
    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final AlmacenamientoArchivos almacenamiento;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerMiPerfil(Long usuarioId) {
        return mapper.toUsuarioResponse(buscar(usuarioId));
    }

    @Override
    @Transactional
    public PreferenciasResponse obtenerPreferencias(Long usuarioId) {
        return mapper.toPreferenciasResponse(preferenciasDe(usuarioId));
    }

    @Override
    @Transactional
    public PreferenciasResponse actualizarPreferencias(Long usuarioId, PreferenciasRequest request) {
        PreferenciasUsuario p = preferenciasDe(usuarioId);
        if (request.recordatoriosReuniones() != null) {
            p.setRecordatoriosReuniones(request.recordatoriosReuniones());
        }
        if (request.avisosProgreso() != null) {
            p.setAvisosProgreso(request.avisosProgreso());
        }
        if (request.notificaciones() != null) {
            p.setNotificaciones(request.notificaciones());
        }
        if (request.idioma() != null && !request.idioma().isBlank()) {
            p.setIdioma(request.idioma().trim().toLowerCase());
        }
        return mapper.toPreferenciasResponse(preferenciasRepository.save(p));
    }

    @Override
    @Transactional
    public UsuarioResponse subirFoto(Long usuarioId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Elige una imagen");
        }
        if (archivo.getSize() > MAX_FOTO_BYTES) {
            throw new ReglaNegocioException("ARCHIVO_GRANDE", "La imagen supera el máximo de 5 MB");
        }
        String extension = extensionPermitida(archivo.getContentType());
        Usuario usuario = buscar(usuarioId);
        try {
            String nombre = almacenamiento.guardar(archivo.getBytes(), extension);
            usuario.setFotoUrl(RUTA_FOTOS + nombre);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer la imagen");
        }
        usuario = usuarioRepository.save(usuario);
        auditoriaService.registrar(usuarioId, AccionAuditoria.PERFIL_ACTUALIZADO, "Usuario", usuarioId,
                "Foto de perfil actualizada");
        return mapper.toUsuarioResponse(usuario);
    }

    private String extensionPermitida(String contentType) {
        if ("image/jpeg".equalsIgnoreCase(contentType) || "image/jpg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        throw new ReglaNegocioException("FORMATO_NO_PERMITIDO", "Solo se aceptan imágenes JPG o PNG");
    }

    private PreferenciasUsuario preferenciasDe(Long usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> preferenciasRepository.save(
                        PreferenciasUsuario.builder().usuario(buscar(usuarioId)).build()));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }
}
