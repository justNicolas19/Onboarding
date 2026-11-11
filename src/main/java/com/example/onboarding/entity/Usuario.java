package com.example.onboarding.entity;



import com.example.onboarding.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Data
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String correo;

    /** Hash BCrypt de la contraseña. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(length = 100)
    private String cargo;

    private LocalDate fechaIngreso;

    /** Identificador del usuario en Moodle, necesario para consultar sus calificaciones. */
    @Column(length = 60)
    private String idMoodle;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /** Manager a cargo. Solo aplica a empleados. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Usuario manager;

    /** Ruta de capacitación asignada. Solo aplica a empleados. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;
}