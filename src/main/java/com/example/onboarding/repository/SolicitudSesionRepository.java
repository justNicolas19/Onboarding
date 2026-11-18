package com.example.onboarding.repository;

import com.example.onboarding.entity.SolicitudSesion;
import com.example.onboarding.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudSesionRepository extends JpaRepository<SolicitudSesion, Long> {

    List<SolicitudSesion> findByManagerIdAndEstadoOrderByFechaDesc(Long managerId, EstadoSolicitud estado);

    List<SolicitudSesion> findByEmpleadoIdAndEstado(Long empleadoId, EstadoSolicitud estado);

    boolean existsByEmpleadoIdAndEstado(Long empleadoId, EstadoSolicitud estado);
}
