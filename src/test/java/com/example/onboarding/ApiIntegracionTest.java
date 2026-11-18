package com.example.onboarding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Recorre con datos demo los flujos que antes no tenían backend (login, líderes, perfil, foto,
 * franjas, solicitudes) y los campos nuevos de las respuestas.
 */
@SpringBootTest
class ApiIntegracionTest {

    private static final String CLAVE = "Onboard2026";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private String token(String correo) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return "Bearer " + extraer(r.getResponse().getContentAsString(), "token");
    }

    private static String extraer(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"?([^\",}]+)").matcher(json);
        if (!m.find()) {
            throw new AssertionError("No se encontró " + campo + " en " + json);
        }
        return m.group(1);
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/progreso/mi-ruta")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginConClaveErradaDevuelveMensajeDelFigma() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"lucia.martinez@empresa.com\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"))
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void recuperarSiempreResponde202() throws Exception {
        mvc.perform(post("/api/auth/recuperar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"nadie@empresa.com\"}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void empleadoRecorreSuRuta() throws Exception {
        String t = token("lucia.martinez@empresa.com");

        mvc.perform(get("/api/usuarios/me").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lucía Martínez"))
                .andExpect(jsonPath("$.managerNombre").value("Elena Torres"))
                .andExpect(jsonPath("$.idMoodle").value("u-lucia"));

        mvc.perform(get("/api/progreso/mi-ruta").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"))
                .andExpect(jsonPath("$[1].tipo").value("LECTURA"))
                .andExpect(jsonPath("$[1].duracionMinutos").value(10))
                .andExpect(jsonPath("$[1].puntosXp").value(300))
                .andExpect(jsonPath("$[1].rutaNombre").value("Bienvenida"))
                .andExpect(jsonPath("$[1].unidadTitulo").value("¡Hola, nuevo comienzo!"))
                .andExpect(jsonPath("$[1].urlMoodle").value("https://moodle.test/mod/page/view.php?id=MOD-02"))
                .andExpect(jsonPath("$[1].evaluacionModuloId").isNumber())
                .andExpect(jsonPath("$[2].estado").value("BLOQUEADO"));

        String ruta = mvc.perform(get("/api/progreso/mi-ruta").header("Authorization", t))
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"moduloId\":(\\d+)").matcher(ruta);
        m.find();
        m.find();
        String normativas = m.group(1);

        // Reportar sin abrir: código estable para la app
        mvc.perform(post("/api/progreso/avance").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moduloId\":" + normativas + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MODULO_NO_ABIERTO"));

        mvc.perform(post("/api/progreso/modulos/" + normativas + "/abrir").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));
        mvc.perform(post("/api/progreso/avance").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moduloId\":" + normativas + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.calificacion").value(100.0));

        mvc.perform(get("/api/lideres").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].nombre").value("Elena Torres"))
                .andExpect(jsonPath("$[0].esMiManager").value(true))
                .andExpect(jsonPath("$[0].area").value("Tecnología"));

        mvc.perform(get("/api/sesiones/mis-sesiones").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.asunto=='Conoce a tu líder')].duracionMinutos", hasItem(30)))
                .andExpect(jsonPath("$[?(@.asunto=='Conoce a tu líder')].managerCargo", hasItem("Líder de Tecnología")))
                .andExpect(jsonPath("$[?(@.asunto=='Conoce a tu líder')].espacioNombre", hasItem("Sala Roble")));

        // La reserva la hizo el manager, pero aparece en "Mis reservas" del empleado
        mvc.perform(get("/api/reservas/mias").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sesionAsunto=='Conoce a tu líder')].espacioNombre", hasItem("Sala Roble")))
                .andExpect(jsonPath("$[?(@.sesionAsunto=='Conoce a tu líder')].sesionManagerNombre", hasItem("Elena Torres")))
                .andExpect(jsonPath("$[?(@.sesionAsunto=='Conoce a tu líder')].espacioCapacidad", hasItem(6)));

        mvc.perform(get("/api/alertas/mias").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].activa").value(true))
                .andExpect(jsonPath("$[*].titulo", hasItem("Completa tu seguimiento")));

        mvc.perform(put("/api/usuarios/me/preferencias").header("Authorization", t)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"avisosProgreso\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avisosProgreso").value(false))
                .andExpect(jsonPath("$.recordatoriosReuniones").value(true));

        mvc.perform(post("/api/sesiones/solicitudes").header("Authorization", t)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        MockMultipartFile foto = new MockMultipartFile("archivo", "yo.png", "image/png", new byte[]{1, 2, 3});
        String perfil = mvc.perform(multipart("/api/usuarios/me/foto").file(foto).header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoUrl", startsWith("/api/archivos/fotos/")))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get(extraer(perfil, "fotoUrl"))).andExpect(status().isOk());

        MockMultipartFile pdf = new MockMultipartFile("archivo", "x.pdf", "application/pdf", new byte[]{1});
        mvc.perform(multipart("/api/usuarios/me/foto").file(pdf).header("Authorization", t))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_NO_PERMITIDO"));

        // Un empleado no puede ver espacios inactivos
        mvc.perform(get("/api/espacios").param("incluirInactivos", "true").header("Authorization", t))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerGestionaSuEquipo() throws Exception {
        String t = token("elena.torres@empresa.com");

        mvc.perform(get("/api/progreso/equipo").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].sesionesRequeridas").value(5))
                .andExpect(jsonPath("$[0].rutaNombre").value("Bienvenida"));

        String equipo = mvc.perform(get("/api/usuarios/mi-equipo").header("Authorization", t))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String empleadoId = extraer(equipo, "id");
        String espacios = mvc.perform(get("/api/espacios").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        String espacioId = extraer(espacios, "id");

        LocalDate pasado = LocalDate.now().plusDays(3);
        mvc.perform(get("/api/reservas/espacio/" + espacioId + "/franjas").param("fecha", pasado.toString())
                        .header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[0].disponible").value(true));

        String fecha = pasado + "T09:00:00";
        mvc.perform(post("/api/sesiones").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empleadoId\":" + empleadoId + ",\"fechaHora\":\"" + fecha + "\",\"espacioId\":"
                                + espacioId + ",\"asunto\":\"Revisión de objetivos\",\"duracionMinutos\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.asunto").value("Revisión de objetivos"))
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.enlaceTeams", startsWith("https://teams.microsoft.com")))
                .andExpect(jsonPath("$.espacioId").value(Integer.parseInt(espacioId)));

        mvc.perform(get("/api/reservas/espacio/" + espacioId + "/franjas").param("fecha", pasado.toString())
                        .header("Authorization", t))
                .andExpect(jsonPath("$[2].disponible").value(false))
                .andExpect(jsonPath("$[2].reservadoPorNombre").value("Elena Torres"))
                .andExpect(jsonPath("$[2].sesionAsunto").value("Revisión de objetivos"));

        mvc.perform(get("/api/alertas/mi-equipo").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void administradorVeBorradoresEInactivos() throws Exception {
        String t = token("admin@empresa.com");

        mvc.perform(post("/api/rutas").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Seguridad y salud\",\"activa\":false}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/rutas").header("Authorization", t))
                .andExpect(jsonPath("$[*].nombre", not(hasItem("Seguridad y salud"))));
        mvc.perform(get("/api/rutas").param("incluirInactivas", "true").header("Authorization", t))
                .andExpect(jsonPath("$[*].nombre", hasItem("Seguridad y salud")))
                .andExpect(jsonPath("$[?(@.nombre=='Bienvenida')].empleadosAsignados", hasItem(3)));

        mvc.perform(get("/api/espacios").param("incluirInactivos", "true").header("Authorization", t))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].ubicacion", hasItem("mantenimiento")));

        String usuarios = mvc.perform(get("/api/usuarios").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idMoodle", hasItem("u-lucia")))
                .andReturn().getResponse().getContentAsString();

        // Hueco 4: editar sin idMoodle ya no lo borra
        Matcher m = Pattern.compile("\\{\"id\":(\\d+),\"nombre\":\"Luis Romero\"").matcher(usuarios);
        m.find();
        String luis = m.group(1);
        mvc.perform(put("/api/usuarios/" + luis).header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Luis Romero\",\"correo\":\"luis.romero@empresa.com\",\"rol\":\"EMPLEADO\","
                                + "\"cargo\":\"Analista senior\",\"area\":\"Tecnología\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMoodle").value("u-luis"))
                .andExpect(jsonPath("$.cargo").value("Analista senior"));

        mvc.perform(get("/api/auditoria").header("Authorization", t))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements", greaterThan(0)));
    }

    @Test
    void usuarioDesactivadoNoPuedeEntrar() throws Exception {
        String admin = token("admin@empresa.com");
        mvc.perform(post("/api/usuarios").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Temporal\",\"correo\":\"temporal@empresa.com\",\"contrasena\":\"" + CLAVE
                                + "\",\"rol\":\"EMPLEADO\"}"))
                .andExpect(status().isCreated());
        String t = token("temporal@empresa.com");
        String yo = mvc.perform(get("/api/usuarios/me").header("Authorization", t))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mvc.perform(delete("/api/usuarios/" + extraer(yo, "id")).header("Authorization", admin))
                .andExpect(status().isNoContent());
        // El token anterior deja de servir y el login se rechaza
        mvc.perform(get("/api/usuarios/me").header("Authorization", t)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"temporal@empresa.com\",\"contrasena\":\"" + CLAVE + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
