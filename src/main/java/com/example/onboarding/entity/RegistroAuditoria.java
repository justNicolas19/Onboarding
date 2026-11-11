package com.example.onboarding.entity;

import com.example.onboarding.enums.AccionAuditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Registro de una acción del proceso. Es de solo inserción: Hibernate no emite
 * UPDATE sobre esta entidad y ningún campo es actualizable (RNF2).
 * La protección definitiva se completa en la base de datos revocando UPDATE y DELETE.
 */
@Entity
@Immutable
@Table(name = "auditoria")
@Data
@Builder
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private AccionAuditoria accion;

    /** Nombre de la entidad afectada, por ejemplo "Sesion" o "ProgresoModulo". */
    @Column(nullable = false, updatable = false, length = 60)
    private String entidad;

    @Column(updatable = false)
    private Long entidadId;

    @Column(updatable = false, length = 1000)
    private String detalle;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    void asignarFecha() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}