package com.example.onboarding.config;

import com.example.onboarding.entity.*;
import com.example.onboarding.enums.*;
import com.example.onboarding.repository.*;
import com.example.onboarding.service.ProgresoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Carga inicial.
 * 1) Crea el primer administrador si no existe ninguno (sin él nadie podría crear usuarios).
 * 2) Solo si onboarding.demo.enabled=true, carga las cuentas del prototipo del Figma
 *    (Lucía Martínez, Elena Torres y Administración) con una ruta, espacios, sesiones y alertas.
 */
@Slf4j
@Configuration
public class DatosInicialesConfig {

    @Bean
    ApplicationRunner datosIniciales(UsuarioRepository usuarios, RutaRepository rutas, ModuloRepository modulos,
                                     EspacioFisicoRepository espacios, SesionSeguimientoRepository sesiones,
                                     AlertaRezagoRepository alertas, ReservaRepository reservas,
                                     ProgresoModuloRepository progresos, ProgresoService progresoService,
                                     PasswordEncoder encoder, TransactionTemplate tx,
                                     @Value("${onboarding.admin.correo:admin@empresa.com}") String adminCorreo,
                                     @Value("${onboarding.admin.contrasena:}") String adminContrasena,
                                     @Value("${onboarding.demo.enabled:false}") boolean demo,
                                     @Value("${onboarding.demo.contrasena:Onboard2026}") String demoContrasena) {
        return args -> tx.executeWithoutResult(status -> {
            if (!usuarios.existsByRol(Rol.ADMINISTRADOR)) {
                String clave = !adminContrasena.isBlank() ? adminContrasena : (demo ? demoContrasena : "");
                if (clave.isBlank()) {
                    log.warn("No hay administrador y onboarding.admin.contrasena está vacía: no se creó ninguno.");
                } else {
                    usuarios.save(Usuario.builder().nombre("Administración").correo(adminCorreo.toLowerCase())
                            .contrasena(encoder.encode(clave)).rol(Rol.ADMINISTRADOR)
                            .cargo("Administrador de OnboardGo").area("Administración").build());
                    log.info("Administrador inicial creado: {}", adminCorreo);
                }
            }
            if (demo && !usuarios.existsByCorreo("lucia.martinez@empresa.com")) {
                cargarDemo(usuarios, rutas, modulos, espacios, sesiones, alertas, reservas, progresos,
                        progresoService, encoder.encode(demoContrasena));
                log.info("Datos de demostración cargados (contraseña de las cuentas demo: onboarding.demo.contrasena)");
            }
        });
    }

    private void cargarDemo(UsuarioRepository usuarios, RutaRepository rutas, ModuloRepository modulos,
                            EspacioFisicoRepository espacios, SesionSeguimientoRepository sesiones,
                            AlertaRezagoRepository alertas, ReservaRepository reservas,
                            ProgresoModuloRepository progresos, ProgresoService progresoService, String hash) {
        Ruta ruta = rutas.save(Ruta.builder().nombre("Bienvenida")
                .descripcion("Tu nueva aventura empieza aquí.").build());
        Modulo cultura = modulos.save(modulo(ruta, 1, "Cultura de la empresa", TipoModulo.VIDEO,
                "Nuestra historia, nuestros valores y la forma en que trabajamos.", "MOD-01", 100));
        Modulo normEval = modulos.save(modulo(ruta, 3, "Normativas · evaluación", TipoModulo.EVALUACION,
                "Pon a prueba lo que aprendiste antes de desbloquear Herramientas.", "MOD-03", 200));
        Modulo normativas = modulo(ruta, 2, "Normativas", TipoModulo.LECTURA,
                "Conoce las políticas que cuidan de ti y del equipo.", "MOD-02", 300);
        normativas.setEvaluacion(normEval);
        modulos.save(normativas);
        modulos.save(modulo(ruta, 4, "Herramientas", TipoModulo.EVALUACION,
                "Conoce las herramientas y los procesos que utilizarás en tu día a día.", "MOD-04", 200));
        modulos.save(modulo(ruta, 5, "Seguridad y salud", TipoModulo.VIDEO,
                "Cómo cuidarte y cuidar a tu equipo en el trabajo.", "MOD-05", 100));
        modulos.save(modulo(ruta, 6, "Beneficios", TipoModulo.LECTURA,
                "Todo lo que la empresa tiene para ti.", "MOD-06", 100));
        modulos.save(modulo(ruta, 7, "Cierre de la bienvenida", TipoModulo.EVALUACION,
                "Repasa lo aprendido y celebra tu primera etapa.", "MOD-07", 300));

        Usuario elena = usuarios.save(Usuario.builder().nombre("Elena Torres").correo("elena.torres@empresa.com")
                .contrasena(hash).rol(Rol.MANAGER).cargo("Líder de Tecnología").area("Tecnología")
                .presentacion("Te acompaña durante la incorporación y te ayuda a resolver dudas sobre el equipo y tus objetivos.")
                .fechaIngreso(LocalDate.of(2021, 3, 1)).build());
        usuarios.save(lider("Ana Ríos", "ana.rios@empresa.com", "Líder de talento", "Recursos Humanos", hash));
        usuarios.save(lider("Carlos Vega", "carlos.vega@empresa.com", "Líder financiero", "Finanzas", hash));
        usuarios.save(lider("Diego Méndez", "diego.mendez@empresa.com", "Líder de operaciones", "Operaciones", hash));
        usuarios.save(lider("Sofía Pérez", "sofia.perez@empresa.com", "Líder de prevención", "Seguridad y Salud", hash));

        Usuario lucia = usuarios.save(empleado("Lucía Martínez", "lucia.martinez@empresa.com",
                "Diseñadora de producto", "Producto", "u-lucia", elena, ruta, hash));
        Usuario ana = usuarios.save(empleado("Ana García", "ana.garcia@empresa.com",
                "Desarrolladora de producto", "Tecnología", "u-ana", elena, ruta, hash));
        usuarios.save(empleado("Luis Romero", "luis.romero@empresa.com",
                "Analista de datos", "Tecnología", "u-luis", elena, ruta, hash));

        EspacioFisico roble = espacios.save(EspacioFisico.builder().nombre("Sala Roble").capacidad(6)
                .ubicacion("videoconferencia").build());
        espacios.save(EspacioFisico.builder().nombre("Sala Cedro").capacidad(10).ubicacion("planta 2").build());
        espacios.save(EspacioFisico.builder().nombre("Auditorio").capacidad(30).ubicacion("mantenimiento")
                .activo(false).build());

        // Lucía ya completó el primer módulo; el segundo queda disponible.
        for (Usuario u : new Usuario[]{lucia, ana}) {
            progresoService.sincronizarRuta(u.getId());
        }
        progresos.findByUsuarioIdAndModuloId(lucia.getId(), cultura.getId()).ifPresent(p -> {
            p.setEstado(EstadoProgreso.COMPLETADO);
            p.setCalificacion(100.0);
            p.setFechaInicio(LocalDateTime.now().minusDays(2));
            p.setFechaFin(LocalDateTime.now().minusDays(1));
        });
        progresos.findByUsuarioIdAndModuloId(lucia.getId(), normativas.getId())
                .ifPresent(p -> p.setEstado(EstadoProgreso.DISPONIBLE));

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(LocalTime.of(10, 30));
        SesionSeguimiento proxima = sesiones.save(SesionSeguimiento.builder().manager(elena).empleado(lucia)
                .fechaHora(manana).duracionMinutos(30).asunto("Conoce a tu líder")
                .descripcion("Hablaremos de tus primeros días, resolveremos dudas y acordaremos tus próximos objetivos.")
                .enlaceTeams("https://teams.microsoft.com/l/meetup-join/demo-conoce-a-tu-lider").build());
        reservas.save(Reserva.builder().espacio(roble).sesion(proxima).reservadoPor(elena)
                .inicio(manana).fin(manana.plusMinutes(30)).build());
        sesiones.save(SesionSeguimiento.builder().manager(elena).empleado(lucia)
                .fechaHora(LocalDate.now().minusDays(2).atTime(11, 0)).duracionMinutos(30)
                .asunto("Seguimiento de la primera semana").estado(EstadoSesion.VERIFICADA)
                .soporteUrl("https://empresa.com/soportes/sesion-1")
                .fechaVerificacion(LocalDateTime.now().minusDays(2).withHour(11).withMinute(40))
                .enlaceTeams("https://teams.microsoft.com/l/meetup-join/demo-primera-semana").build());

        alertas.save(AlertaRezago.builder().empleado(lucia).nivel(NivelAlerta.ALTO)
                .titulo("Completa tu seguimiento").motivo("Falta una sesión verificada para esta semana.").build());
        alertas.save(AlertaRezago.builder().empleado(lucia).nivel(NivelAlerta.MEDIO)
                .titulo("Normativas te está esperando").motivo("Aún no reportas avance en los últimos 3 días.").build());
        alertas.save(AlertaRezago.builder().empleado(lucia).nivel(NivelAlerta.BAJO)
                .titulo("Una nueva oportunidad de aprender").motivo("Tienes contenido disponible en tu ruta.").build());
    }

    private Modulo modulo(Ruta ruta, int orden, String titulo, TipoModulo tipo, String descripcion,
                          String idMoodle, int xp) {
        return Modulo.builder().ruta(ruta).orden(orden).titulo(titulo).tipo(tipo).descripcion(descripcion)
                .idMoodle(idMoodle).puntajeMinimo(70).duracionMinutos(10).puntosXp(xp)
                .unidad(1).unidadTitulo("¡Hola, nuevo comienzo!").build();
    }

    private Usuario lider(String nombre, String correo, String cargo, String area, String hash) {
        return Usuario.builder().nombre(nombre).correo(correo).contrasena(hash).rol(Rol.MANAGER)
                .cargo(cargo).area(area).build();
    }

    private Usuario empleado(String nombre, String correo, String cargo, String area, String idMoodle,
                             Usuario manager, Ruta ruta, String hash) {
        return Usuario.builder().nombre(nombre).correo(correo).contrasena(hash).rol(Rol.EMPLEADO)
                .cargo(cargo).area(area).idMoodle(idMoodle).manager(manager).ruta(ruta)
                .fechaIngreso(LocalDate.now().minusDays(7)).build();
    }
}
