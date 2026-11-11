package com.example.onboarding.repository;

import com.example.onboarding.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /** Indica si el espacio ya está reservado en algún momento del rango pedido. */
    @Query("""
            select count(r) > 0 from Reserva r
            where r.espacio.id = :espacioId
              and r.inicio < :fin
              and r.fin > :inicio
            """)
    boolean existeCruce(@Param("espacioId") Long espacioId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

    Optional<Reserva> findBySesionId(Long sesionId);

    List<Reserva> findByReservadoPorIdOrderByInicioDesc(Long usuarioId);

    List<Reserva> findByEspacioIdAndInicioBetweenOrderByInicioAsc(Long espacioId, LocalDateTime desde, LocalDateTime hasta);
}
