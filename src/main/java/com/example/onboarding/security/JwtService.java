package com.example.onboarding.security;

import com.example.onboarding.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Firma y valida los tokens del login (hueco 1 del reporte). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long horasExpiracion;

    public JwtService(@Value("${onboarding.jwt.secret}") String secretoBase64,
                      @Value("${onboarding.jwt.expiracion-horas:8}") long horasExpiracion) {
        byte[] bytes = Decoders.BASE64.decode(secretoBase64);
        if (bytes.length < 32) {
            throw new IllegalStateException("onboarding.jwt.secret debe tener al menos 256 bits (32 bytes en Base64)");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.horasExpiracion = horasExpiracion;
    }

    public record TokenGenerado(String valor, LocalDateTime expiraEn) {}

    /** El id del usuario va en "sub"; el rol y el nombre van como claims. */
    public TokenGenerado generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(horasExpiracion, ChronoUnit.HOURS);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new TokenGenerado(token, LocalDateTime.ofInstant(expira, ZoneId.systemDefault()));
    }

    /** Devuelve el id del usuario del token, o null si el token no es válido o expiró. */
    public Long validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
