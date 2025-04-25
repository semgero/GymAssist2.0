package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mediciones")  // Nombre de la tabla en MySQL
public class MedicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double estatura; // en metros

    @Column(nullable = false)
    private double peso;     // en kg

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Relación Many-to-One con ClientEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)  // Columna FK en la tabla 'mediciones'
    private ClientEntity cliente;
}