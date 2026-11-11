package com.example.onboarding.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "espacios")
@Data
@Builder
public class EspacioFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}