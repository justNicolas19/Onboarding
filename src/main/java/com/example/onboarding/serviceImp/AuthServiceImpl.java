package com.example.onboarding.serviceImp;

import com.example.onboarding.dto.request.LoginRequest;
import com.example.onboarding.dto.response.AuthResponse;
import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.exception.CredencialesInvalidasException;
import com.example.onboarding.integration.CorreoClient;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.security.JwtService;
import com.example.onboarding.service.AuditoriaService;
import com.example.onboarding.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Texto del Figma ("Error de acceso"). Es el mismo para correo inexistente y contraseña errada. */
    static final String CREDENCIALES_INCORRECTAS = "Correo o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    private final CorreoClient correoClient;

    @Override
    @Transactional
    public AuthResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo().trim().toLowerCase())
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS));
        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INCORRECTAS);
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new CredencialesInvalidasException("La cuenta está desactivada");
        }
        JwtService.TokenGenerado token = jwtService.generar(usuario);
        auditoriaService.registrar(usuario.getId(), AccionAuditoria.INICIO_SESION, "Usuario", usuario.getId(),
                "Inicio de sesión");
        return new AuthResponse(token.valor(), token.expiraEn(), usuario.getId(), usuario.getNombre(),
                usuario.getRol());
    }

    @Override
    @Transactional(readOnly = true)
    public void solicitarRecuperacion(String correo) {
        usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .ifPresent(u -> correoClient.enviarRecuperacion(u.getCorreo(), u.getNombre()));
    }
}
