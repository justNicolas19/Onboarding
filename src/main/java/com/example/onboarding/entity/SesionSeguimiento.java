package com.example.onboarding.entity;

import com.example.onboarding.enums.EstadoSesion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionSeguimiento {

    public static final int DURACION_POR_DEFECTO = 60;
    public static final String ASUNTO_POR_DEFECTO = "Seguimiento de incorporación";

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

    /* Campos nuevos (sección 4 del reporte), nullable para ddl-auto=update. */

    /** Asunto de la sesión ("Conoce a tu líder"). */
    @Column(length = 150)
    private String asunto;

    /** Lo que se hablará en la sesión. */
    @Column(length = 500)
    private String descripcion;

    /** Duración en minutos. Antes era una constante de 1 hora. */
    private Integer duracionMinutos;

    /** Observaciones que el manager deja al adjuntar el soporte. */
    @Column(length = 500)
    private String observaciones;

    /** Momento en que se adjuntó el soporte y la sesión quedó VERIFICADA. */
    private LocalDateTime fechaVerificacion;

    public int duracionEfectiva() {
        return duracionMinutos != null ? duracionMinutos : DURACION_POR_DEFECTO;
    }

    public LocalDateTime fechaFin() {
        return fechaHora.plusMinutes(duracionEfectiva());
    }
}
