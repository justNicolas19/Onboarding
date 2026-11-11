package com.example.onboarding.repository;

import com.example.onboarding.entity.ProgresoModulo;
import com.example.onboarding.enums.EstadoProgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgresoModuloRepository extends JpaRepository<ProgresoModulo, Long> {

    /** Ruta del empleado con el estado de cada módulo, en orden. */
    List<ProgresoModulo> findByUsuarioIdOrderByModuloOrdenAsc(Long usuarioId);

    Optional<ProgresoModulo> findByUsuarioIdAndModuloId(Long usuarioId, Long moduloId);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoProgreso estado);

    long countByUsuarioId(Long usuarioId);

    /** Promedio de calificaciones del empleado; null si aún no tiene ninguna. */
    @Query("""
            select avg(p.calificacion) from ProgresoModulo p
            where p.usuario.id = :usuarioId and p.calificacion is not null
            """)
    Double promedioCalificacion(@Param("usuarioId") Long usuarioId);

    boolean existsByModuloId(Long moduloId);
}
