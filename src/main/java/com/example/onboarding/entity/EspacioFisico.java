package com.example.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "espacios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    /** Ubicación o nota del espacio: "planta 2", "videoconferencia", "mantenimiento". */
    @Column(length = 150)
    private String ubicacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
