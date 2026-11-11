package com.example.onboarding.entity;


import com.example.onboarding.enums.EstadoSesion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
@Data
@Builder
public class SesionSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Usuario manager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Usuario empleado;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /** Enlace de la reunión creada en Teams. */
    @Column(length = 500)
    private String enlaceTeams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSesion estado = EstadoSesion.PROGRAMADA;

    /** Ubicación del archivo que demuestra que la sesión se realizó. */
    @Column(length = 500)
    private String soporteUrl;
}