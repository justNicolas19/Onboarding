package com.example.onboarding.entity;

import com.example.onboarding.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Pedido de un empleado a su manager para que le agende una sesión ("Agendar una sesión"). */
@Entity
@Table(name = "solicitudes_sesion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Usuario empleado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Usuario manager;

    /** Fecha que propone el empleado. Opcional: el manager decide. */
    private LocalDateTime fechaPropuesta;

    @Column(length = 300)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void asignarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
