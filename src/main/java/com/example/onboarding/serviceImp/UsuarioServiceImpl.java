package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.UsuarioRequest;
import com.example.onboarding.dto.response.UsuarioResponse;
import com.example.onboarding.entity.Ruta;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.exception.RecursoNoEncontradoException;
import com.example.onboarding.exception.ReglaNegocioException;
import com.example.onboarding.mapper.EntityMapper;
import com.example.onboarding.repository.RutaRepository;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.ProgresoService;
import com.example.onboarding.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {


    private final UsuarioRepository usuarioRepository;
    private final RutaRepository rutaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProgresoService progresoService;
    private final AuditoriaService auditoriaService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public UsuarioResponse crear(Long actorId, UsuarioRequest request) {
        String correo = normalizar(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        if (request.contrasena() == null || request.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .correo(correo)
                .contrasena(passwordEncoder.encode(request.contrasena()))
                .rol(request.rol())
                .cargo(request.cargo())
                .fechaIngreso(request.fechaIngreso())
                .idMoodle(request.idMoodle())
                .build();
        asignarRelaciones(usuario, request);
        usuario = usuarioRepository.save(usuario);

        if (usuario.getRuta() != null) {
            progresoService.sincronizarRuta(usuario.getId());
        }
        auditoriaService.registrar(actorId, AccionAuditoria.USUARIO_CREADO, "Usuario", usuario.getId(),
                "Usuario creado: " + usuario.getCorreo());
        return mapper.toUsuarioResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long actorId, Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        String correo = normalizar(request.correo());
        if (!usuario.getCorreo().equalsIgnoreCase(correo) && usuarioRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
        Long rutaAnterior = usuario.getRuta() != null ? usuario.getRuta().getId() : null;

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRol(request.rol());
        usuario.setCargo(request.cargo());
        usuario.setFechaIngreso(request.fechaIngreso());
        usuario.setIdMoodle(request.idMoodle());
        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        }
        asignarRelaciones(usuario, request);
        usuario = usuarioRepository.save(usuario);

        Long rutaNueva = usuario.getRuta() != null ? usuario.getRuta().getId() : null;
        if (rutaNueva != null && !Objects.equals(rutaAnterior, rutaNueva)) {
            progresoService.sincronizarRuta(usuario.getId());
            auditoriaService.registrar(actorId, AccionAuditoria.RUTA_ASIGNADA, "Usuario", usuario.getId(),
                    "Ruta asignada: " + usuario.getRuta().getNombre());
        }
        auditoriaService.registrar(actorId, AccionAuditoria.USUARIO_MODIFICADO, "Usuario", usuario.getId(),
                "Usuario modificado: " + usuario.getCorreo());
        return mapper.toUsuarioResponse(usuario);
    }

    @Override
    @Transactional
    public void desactivar(Long actorId, Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(actorId, AccionAuditoria.USUARIO_MODIFICADO, "Usuario", id,
                "Usuario desactivado: " + usuario.getCorreo());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return mapper.toUsuarioResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(mapper::toUsuarioResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarEmpleadosDeManager(Long managerId) {
        return usuarioRepository.findByManagerIdAndActivoTrue(managerId).stream()
                .map(mapper::toUsuarioResponse).toList();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }

    private void asignarRelaciones(Usuario usuario, UsuarioRequest request) {
        if (request.managerId() != null) {
            Usuario manager = buscar(request.managerId());
            if (manager.getRol() != Rol.MANAGER) {
                throw new ReglaNegocioException("El usuario indicado como manager no tiene rol MANAGER");
            }
            usuario.setManager(manager);
        } else {
            usuario.setManager(null);
        }

        if (request.rutaId() != null) {
            if (usuario.getRol() != Rol.EMPLEADO) {
                throw new ReglaNegocioException("Solo los empleados tienen una ruta asignada");
            }
            Ruta ruta = rutaRepository.findById(request.rutaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada: " + request.rutaId()));
            usuario.setRuta(ruta);
        } else {
            usuario.setRuta(null);
        }
    }
}
