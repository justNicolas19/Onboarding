package com.example.onboarding.integration;

import com.example.onboarding.exception.IntegracionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "moodle", name = "modo", havingValue = "real")
public class MoodleClientReal implements MoodleClient {

    private static final String FUNCION = "gradereport_user_get_grade_items";
    private static final ParameterizedTypeReference<Map<String, Object>> TIPO_MAPA = new ParameterizedTypeReference<>() {};

    private final RestClient cliente;
    private final String token;

    public MoodleClientReal(@Value("${moodle.url:}") String url,
                            @Value("${moodle.token:}") String token) {
        if (url.isBlank() || token.isBlank()) {
            throw new IllegalStateException("moodle.modo=real exige configurar MOODLE_URL y MOODLE_TOKEN");
        }
        this.token = token;
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.cliente = RestClient.builder()
                .baseUrl(url.replaceAll("/+$", ""))
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Double consultarCalificacion(String idUsuarioMoodle, String idActividadMoodle) {
        String[] partes = idActividadMoodle.split(":", 2);
        String cursoId = partes[0].trim();
        String actividadId = partes.length == 2 ? partes[1].trim() : null;

        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("wstoken", token);
        formulario.add("wsfunction", FUNCION);
        formulario.add("moodlewsrestformat", "json");
        formulario.add("courseid", cursoId);
        formulario.add("userid", idUsuarioMoodle);

        Map<String, Object> respuesta;
        try {
            respuesta = cliente.post()
                    .uri("/webservice/rest/server.php")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulario)
                    .retrieve()
                    .body(TIPO_MAPA);
        } catch (RestClientException e) {
            throw new IntegracionException("No se pudo consultar Moodle", e);
        }
        if (respuesta == null) {
            throw new IntegracionException("Moodle respondió sin contenido");
        }
        if (respuesta.containsKey("exception") || respuesta.containsKey("errorcode")) {
            log.warn("Moodle devolvió un error: {} - {}", respuesta.get("errorcode"), respuesta.get("message"));
            throw new IntegracionException("Moodle rechazó la consulta: " + respuesta.get("message"));
        }
        return extraerNota(respuesta, cursoId, actividadId);
    }

    @SuppressWarnings("unchecked")
    private Double extraerNota(Map<String, Object> respuesta, String cursoId, String actividadId) {
        Object notasUsuario = respuesta.get("usergrades");
        if (!(notasUsuario instanceof List<?> lista) || lista.isEmpty()) {
            return null;
        }
        Object items = ((Map<String, Object>) lista.get(0)).get("gradeitems");
        if (!(items instanceof List<?> elementos)) {
            return null;
        }
        for (Object elemento : elementos) {
            Map<String, Object> item = (Map<String, Object>) elemento;
            boolean coincide = actividadId != null
                    ? actividadId.equals(String.valueOf(item.get("cmid")))
                    : "course".equals(item.get("itemtype"));
            if (!coincide) {
                continue;
            }
            Double nota = numero(item.get("graderaw"));
            Double maximo = numero(item.get("grademax"));
            if (nota == null) {
                return null; // todavía no hay calificación
            }
            return (maximo != null && maximo > 0) ? nota / maximo * 100.0 : nota;
        }
        if (actividadId != null) {
            throw new IntegracionException("La actividad " + actividadId + " no existe en el curso " + cursoId
                    + " de Moodle");
        }
        return null;
    }

    private Double numero(Object valor) {
        if (valor instanceof Number n) {
            return n.doubleValue();
        }
        if (valor instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
