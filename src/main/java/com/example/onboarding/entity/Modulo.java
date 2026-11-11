package com.example.onboarding.entity;


import com.example.onboarding.enums.TipoModulo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modulos", uniqueConstraints = @UniqueConstraint(name = "uk_modulo_ruta_orden", columnNames = {"ruta_id", "orden"}))
@Data
@Builder
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoModulo tipo;

    /** Posición dentro de la ruta. Un módulo se desbloquea al completar el anterior. */
    @Column(nullable = false)
    private Integer orden;

    /** Identificador del curso o actividad en Moodle. */
    @Column(nullable = false, length = 60)
    private String idMoodle;

    /** Calificación mínima (0 a 100) para dar el módulo por completado. */
    @Column(nullable = false)
    @Builder.Default
    private Integer puntajeMinimo = 70;
}