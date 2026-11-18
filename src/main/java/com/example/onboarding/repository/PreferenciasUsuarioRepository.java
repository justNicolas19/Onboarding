package com.example.onboarding.repository;

import com.example.onboarding.entity.PreferenciasUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciasUsuarioRepository extends JpaRepository<PreferenciasUsuario, Long> {

    Optional<PreferenciasUsuario> findByUsuarioId(Long usuarioId);
}
