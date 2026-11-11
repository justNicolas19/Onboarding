package com.example.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "reservas")
@Data
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private EspacioFisico espacio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", unique = true)
    private SesionSeguimiento sesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservado_por_id", nullable = false)
    private Usuario reservadoPor;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;
}
