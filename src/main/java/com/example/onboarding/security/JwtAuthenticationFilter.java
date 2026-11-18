package com.example.onboarding.security;

import com.example.onboarding.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer ..." y deja el id del usuario como nombre del principal
 * (lo que espera SecurityUtils.usuarioActualId) y ROLE_<rol> como authority.
 * El rol y el estado se leen de la base: un usuario desactivado pierde el acceso de inmediato.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            Long usuarioId = jwtService.validar(cabecera.substring(PREFIJO.length()).trim());
            if (usuarioId != null) {
                usuarioRepository.findById(usuarioId)
                        .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                        .ifPresent(u -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    String.valueOf(u.getId()), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }
}
