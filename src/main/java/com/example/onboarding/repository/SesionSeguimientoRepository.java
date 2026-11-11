package com.example.onboarding.repository;

import com.example.onboarding.entity.SesionSeguimiento;
import com.example.onboarding.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionSeguimientoRepository extends JpaRepository<SesionSeguimiento, Long> {

    List<SesionSeguimiento> findByEmpleadoIdOrderByFechaHoraDesc(Long empleadoId);

    List<SesionSeguimiento> findByManagerIdOrderByFechaHoraDesc(Long managerId);

    /** Sesiones que cuentan para la auditoría (mínimo 5). */
    long countByEmpleadoIdAndEstado(Long empleadoId, EstadoSesion estado);

    /** Sesiones agendadas sin contar las canceladas, para respetar el máximo de 10. */
    long countByEmpleadoIdAndEstadoNot(Long empleadoId, EstadoSesion estado);

    /** Evita que el manager tenga dos sesiones a la misma hora. */
    boolean existsByManagerIdAndFechaHoraAndEstado(Long managerId, LocalDateTime fechaHora, EstadoSesion estado);

    /** Evidencia para la planilla de cumplimiento (RF12). */
    List<SesionSeguimiento> findByEmpleadoIdAndEstadoOrderByFechaHoraAsc(Long empleadoId, EstadoSesion estado);
}
