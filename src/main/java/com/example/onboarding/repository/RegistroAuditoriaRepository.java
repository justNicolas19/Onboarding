package com.example.onboarding.repository;

import com.example.onboarding.entity.RegistroAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;

/**
 * Extiende Repository y no JpaRepository a propósito: así no existen delete ni deleteAll,
 * y la auditoría queda de solo inserción y consulta (RNF2).
 */
public interface RegistroAuditoriaRepository extends Repository<RegistroAuditoria, Long> {

    <S extends RegistroAuditoria> S save(S registro);

    Page<RegistroAuditoria> findAll(Pageable pageable);

    Page<RegistroAuditoria> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<RegistroAuditoria> findByEntidadAndEntidadId(String entidad, Long entidadId, Pageable pageable);

    Page<RegistroAuditoria> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
}
