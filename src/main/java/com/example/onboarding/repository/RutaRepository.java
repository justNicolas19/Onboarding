package com.example.onboarding.repository;

import com.example.onboarding.entity.Ruta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    List<Ruta> findByActivaTrue();

    /** Carga la ruta junto con sus módulos en una sola consulta. */
    @EntityGraph(attributePaths = "modulos")
    Optional<Ruta> findConModulosById(Long id);
}
