package com.example.onboarding.security;

import com.example.onboarding.entity.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Genera y valida los tokens JWT (HS256). El subject del token es el id del usuario (RNF1). */
@Component
public class JwtService {

    private static final String EMISOR = "onboarding";
    public static final String CLAIM_ROL = "rol";

    private final byte[] secreto;
    private final MACSigner firmador;
    private final long expiracionMinutos;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiracion-minutos:480}") long expiracionMinutos) {
        this.secreto = secret.getBytes(StandardCharsets.UTF_8);
        if (secreto.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres");
        }
        try {
            this.firmador = new MACSigner(secreto);
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo inicializar la firma del JWT", e);
        }
        this.expiracionMinutos = expiracionMinutos;
    }

    public record TokenGenerado(String token, LocalDateTime expiraEn) {}

    public TokenGenerado generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(EMISOR)
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROL, usuario.getRol().name())
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(expira))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(firmador);
            return new TokenGenerado(jwt.serialize(), LocalDateTime.ofInstant(expira, ZoneId.systemDefault()));
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }

    /** Devuelve los claims solo si la firma, el emisor y la expiración son válidos. */
    public Optional<JWTClaimsSet> validar(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                return Optional.empty();
            }
            if (!jwt.verify(new MACVerifier(secreto))) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expira = claims.getExpirationTime();
            if (expira == null || expira.before(new Date()) || !EMISOR.equals(claims.getIssuer())) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (ParseException | JOSEException e) {
            return Optional.empty();
        }
    }
}
