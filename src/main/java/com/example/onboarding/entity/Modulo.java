package com.example.onboarding.entity;

import com.example.onboarding.enums.TipoModulo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modulos", uniqueConstraints = @UniqueConstraint(name = "uk_modulo_ruta_orden", columnNames = {"ruta_id", "orden"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modulo {

    public static final int DURACION_POR_DEFECTO = 10;
    public static final int XP_POR_DEFECTO = 100;
    public static final int UNIDAD_POR_DEFECTO = 1;

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

    /*
     * Campos nuevos (sección 4 del reporte). Son nullable en la base para que
     * ddl-auto=update pueda agregarlos sobre tablas con datos; el mapper aplica los valores por defecto.
     */

    /** Duración estimada en minutos ("10 min" en el Figma). */
    private Integer duracionMinutos;

    /** Puntos de experiencia al completar el módulo ("+300 XP"). */
    private Integer puntosXp;

    /** Número de unidad dentro de la ruta ("UNIDAD 1"). */
    private Integer unidad;

    /** Título de la unidad ("¡Hola, nuevo comienzo!"). */
    @Column(length = 120)
    private String unidadTitulo;

    /** Evaluación asociada a este módulo de contenido ("Ir a la evaluación"). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_id")
    private Modulo evaluacion;
}
