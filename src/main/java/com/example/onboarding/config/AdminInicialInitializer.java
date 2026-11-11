package com.example.onboarding.config;

import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.AccionAuditoria;
import com.example.onboarding.enums.Rol;
import com.example.onboarding.repository.UsuarioRepository;
import com.example.onboarding.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicialInitializer implements ApplicationRunner {

    private static final int MIN_CONTRASENA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Value("${admin.inicial.correo:}")
    private String correo;

    @Value("${admin.inicial.contrasena:}")
    private String contrasena;

    @Value("${admin.inicial.nombre:Administrador}")
    private String nombre;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
            return;
        }
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            log.warn("No hay ningún administrador y no se definieron ADMIN_CORREO / ADMIN_CONTRASENA: "
                    + "nadie podrá crear usuarios hasta que se configuren.");
            return;
        }
        if (contrasena.length() < MIN_CONTRASENA) {
            throw new IllegalStateException("ADMIN_CONTRASENA debe tener al menos " + MIN_CONTRASENA + " caracteres");
        }

        try {
            Usuario admin = usuarioRepository.save(Usuario.builder()
                    .nombre(nombre.trim())
                    .correo(correo.trim().toLowerCase())
                    .contrasena(passwordEncoder.encode(contrasena))
                    .rol(Rol.ADMINISTRADOR)
                    .cargo("Administrador")
                    .fechaIngreso(LocalDate.now())
                    .build());
            auditoriaService.registrar(admin.getId(), AccionAuditoria.USUARIO_CREADO, "Usuario", admin.getId(),
                    "Administrador inicial creado al arrancar: " + admin.getCorreo());
            log.info("Administrador inicial creado: {}", admin.getCorreo());
        } catch (DataIntegrityViolationException e) {
            log.warn("No se pudo crear el administrador inicial (¿el correo ya existe?): {}", e.getMessage());
        }
    }
}
