package com.example.onboarding.security;

import com.example.onboarding.enums.Rol;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validar(encabezado.substring(PREFIJO.length()).trim()).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(JWTClaimsSet claims) {
        try {
            Long.parseLong(claims.getSubject());
            Rol rol = Rol.valueOf(String.valueOf(claims.getClaim(JwtService.CLAIM_ROL)));
            var autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                    claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (IllegalArgumentException e) {
            // Token firmado pero con datos inesperados (id no numérico, rol desconocido): no autentica.
        }
    }
}
