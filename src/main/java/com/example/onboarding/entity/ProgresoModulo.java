package com.example.onboarding.entity;


import com.example.onboarding.enums.EstadoProgreso;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Avance de un empleado en un módulo. Hay una sola fila por pareja empleado-módulo. */
@Entity
@Table(name = "progreso_modulo", uniqueConstraints = @UniqueConstraint(name = "uk_progreso_usuario_modulo", columnNames = {"usuario_id", "modulo_id"}))
@Data
@Builder
public class ProgresoModulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoProgreso estado = EstadoProgreso.BLOQUEADO;

    /** Última calificación reportada por Moodle (0 a 100). */
    private Double calificacion;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;
}