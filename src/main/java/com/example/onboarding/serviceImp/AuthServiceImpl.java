package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.LoginRequest;
import com.example.onboarding.dto.response.AuthResponse;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.exception.CredencialesInvalidasException;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.security.JwtService;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthServiceImpl implements AuthService {

    /** BCrypt solo usa los primeros 72 bytes; más que eso no puede ser una contraseña válida del sistema. */
    private static final int MAX_BYTES_CONTRASENA = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    /** Se compara contra este hash cuando el correo no existe, para que la respuesta tarde lo mismo. */
    private final String hashFalso;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
        this.hashFalso = passwordEncoder.encode("contrasena-inexistente");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request.contrasena().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_CONTRASENA) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = usuarioRepository.findByCorreo(request.correo().trim().toLowerCase()).orElse(null);
        String hash = usuario != null ? usuario.getContrasena() : hashFalso;
        boolean coincide = passwordEncoder.matches(request.contrasena(), hash);

        if (usuario == null || !coincide || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new CredencialesInvalidasException();
        }

        JwtService.TokenGenerado token = jwtService.generar(usuario);
        auditoriaService.registrar(usuario.getId(), AccionAuditoria.INICIO_SESION, "Usuario", usuario.getId(),
                "Inicio de sesión de " + usuario.getCorreo());
        return new AuthResponse(token.token(), token.expiraEn(), usuario.getId(), usuario.getNombre(),
                usuario.getRol());
    }
}
