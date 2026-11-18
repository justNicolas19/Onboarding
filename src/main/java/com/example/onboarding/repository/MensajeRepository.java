package com.example.onboarding.repository;

import com.example.onboarding.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByDestinatarioIdOrderByFechaDesc(Long destinatarioId);
}
