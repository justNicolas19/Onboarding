package com.example.onboarding.repository;

import com.example.onboarding.entity.AlertaRezago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaRezagoRepository extends JpaRepository<AlertaRezago, Long> {

    List<AlertaRezago> findByEmpleadoIdOrderByFechaDesc(Long empleadoId);

    /** Alertas de todos los empleados de un manager. */
    List<AlertaRezago> findByEmpleadoManagerIdOrderByFechaDesc(Long managerId);

    /** Sirve para marcar "con alerta reciente" en el panel y para no duplicar alertas. */
    boolean existsByEmpleadoIdAndFechaAfter(Long empleadoId, LocalDateTime desde);
}
