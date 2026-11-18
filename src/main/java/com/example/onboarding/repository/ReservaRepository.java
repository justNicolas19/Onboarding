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

    /**
     * Reservas que hizo el usuario más las reservas de las sesiones donde participa como empleado.
     * Así "Mis reservas" del empleado muestra la sala de su sesión de seguimiento.
     */
    @Query("""
            select r from Reserva r
            left join r.sesion s
            where r.reservadoPor.id = :usuarioId or s.empleado.id = :usuarioId
            order by r.inicio desc
            """)
    List<Reserva> findDelUsuarioOSusSesiones(@Param("usuarioId") Long usuarioId);

    List<Reserva> findByEspacioIdAndInicioBetweenOrderByInicioAsc(Long espacioId, LocalDateTime desde, LocalDateTime hasta);

    /** Reservas que se cruzan con el rango, para pintar las franjas ocupadas. */
    @Query("""
            select r from Reserva r
            where r.espacio.id = :espacioId
              and r.inicio < :hasta
              and r.fin > :desde
            order by r.inicio asc
            """)
    List<Reserva> findCruzadas(@Param("espacioId") Long espacioId,
                               @Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta);
}
