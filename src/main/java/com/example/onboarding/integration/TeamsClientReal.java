package com.example.onboarding.integration;

import com.example.onboarding.exception.IntegracionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "teams", name = "modo", havingValue = "real")
public class TeamsClientReal implements TeamsClient {

    private static final ParameterizedTypeReference<Map<String, Object>> TIPO_MAPA = new ParameterizedTypeReference<>() {};
    private static final DateTimeFormatter FORMATO_GRAPH = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RestClient graph;
    private final RestClient login;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String zonaHoraria;

    private String tokenEnCache;
    private Instant tokenExpira = Instant.EPOCH;

    public TeamsClientReal(@Value("${teams.tenant-id:}") String tenantId,
                           @Value("${teams.client-id:}") String clientId,
                           @Value("${teams.client-secret:}") String clientSecret,
                           @Value("${teams.zona-horaria:SA Pacific Standard Time}") String zonaHoraria) {
        if (tenantId.isBlank() || clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "teams.modo=real exige configurar TEAMS_TENANT_ID, TEAMS_CLIENT_ID y TEAMS_CLIENT_SECRET");
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.zonaHoraria = zonaHoraria;

        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        fabrica.setReadTimeout(Duration.ofSeconds(15));
        this.graph = RestClient.builder().baseUrl("https://graph.microsoft.com/v1.0").requestFactory(fabrica).build();
        this.login = RestClient.builder().baseUrl("https://login.microsoftonline.com").requestFactory(fabrica).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String crearReunion(String asunto, LocalDateTime inicio, LocalDateTime fin,
                               String correoManager, String correoEmpleado) {
        Map<String, Object> evento = Map.of(
                "subject", asunto,
                "start", Map.of("dateTime", inicio.withNano(0).format(FORMATO_GRAPH), "timeZone", zonaHoraria),
                "end", Map.of("dateTime", fin.withNano(0).format(FORMATO_GRAPH), "timeZone", zonaHoraria),
                "attendees", List.of(Map.of(
                        "emailAddress", Map.of("address", correoEmpleado),
                        "type", "required")),
                "isOnlineMeeting", true,
                "onlineMeetingProvider", "teamsForBusiness");

        Map<String, Object> respuesta;
        try {
            respuesta = graph.post()
                    .uri("/users/{correo}/events", correoManager)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtenerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(evento)
                    .retrieve()
                    .body(TIPO_MAPA);
        } catch (RestClientException e) {
            throw new IntegracionException("No se pudo crear la reunión en Teams", e);
        }

        Object reunion = respuesta != null ? respuesta.get("onlineMeeting") : null;
        Object enlace = reunion instanceof Map<?, ?> mapa ? mapa.get("joinUrl") : null;
        if (enlace == null) {
            throw new IntegracionException("Microsoft Graph creó el evento pero no devolvió el enlace de Teams");
        }
        return enlace.toString();
    }

    /** Token de aplicación; se reutiliza hasta un minuto antes de vencer. */
    private synchronized String obtenerToken() {
        if (tokenEnCache != null && Instant.now().isBefore(tokenExpira.minusSeconds(60))) {
            return tokenEnCache;
        }
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("client_id", clientId);
        formulario.add("client_secret", clientSecret);
        formulario.add("scope", "https://graph.microsoft.com/.default");
        formulario.add("grant_type", "client_credentials");

        try {
            Map<String, Object> respuesta = login.post()
                    .uri("/{tenant}/oauth2/v2.0/token", tenantId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulario)
                    .retrieve()
                    .body(TIPO_MAPA);
            if (respuesta == null || respuesta.get("access_token") == null) {
                throw new IntegracionException("Microsoft no entregó un token de acceso");
            }
            long segundos = respuesta.get("expires_in") instanceof Number n ? n.longValue() : 3600;
            tokenEnCache = respuesta.get("access_token").toString();
            tokenExpira = Instant.now().plusSeconds(segundos);
            return tokenEnCache;
        } catch (RestClientException e) {
            throw new IntegracionException("No se pudo autenticar la app contra Microsoft", e);
        }
    }
}
