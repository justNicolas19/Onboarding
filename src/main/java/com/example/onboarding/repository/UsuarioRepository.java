package com.example.onboarding.repository;

import com.example.onboarding.entity.Usuario;
import com.example.onboarding.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Login y validación de correo único. */
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    /** Empleados a cargo de un manager (panel de seguimiento, RF7). */
    List<Usuario> findByManagerIdAndActivoTrue(Long managerId);

    List<Usuario> findByRol(Rol rol);
}
