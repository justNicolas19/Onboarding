package com.example.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mensaje directo entre un empleado y un líder ("Enviar mensaje"). */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void asignarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
