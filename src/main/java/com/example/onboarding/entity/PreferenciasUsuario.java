package com.example.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

/** Preferencias de la sección "Configuración" de Mi perfil. Una fila por usuario. */
@Entity
@Table(name = "preferencias_usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciasUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recordatoriosReuniones = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean avisosProgreso = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificaciones = true;

    /** Código ISO del idioma, por ejemplo "es". */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String idioma = "es";
}
