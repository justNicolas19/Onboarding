package com.example.onboarding.repository;

import com.example.onboarding.entity.EspacioFisico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspacioFisicoRepository extends JpaRepository<EspacioFisico, Long> {

    List<EspacioFisico> findByActivoTrue();
}
