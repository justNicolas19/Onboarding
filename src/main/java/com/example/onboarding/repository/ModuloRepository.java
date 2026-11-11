package com.example.onboarding.repository;

import com.example.onboarding.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    List<Modulo> findByRutaIdOrderByOrdenAsc(Long rutaId);

    boolean existsByRutaIdAndOrden(Long rutaId, Integer orden);

    /** Módulo que sigue al actual: es el que se desbloquea al completar este (RF2). */
    Optional<Modulo> findFirstByRutaIdAndOrdenGreaterThanOrderByOrdenAsc(Long rutaId, Integer orden);

    /** Primer módulo de la ruta, el que queda disponible al asignarla. */
    Optional<Modulo> findFirstByRutaIdOrderByOrdenAsc(Long rutaId);

    long countByRutaId(Long rutaId);
}
