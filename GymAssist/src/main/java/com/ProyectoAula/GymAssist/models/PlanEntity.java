package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "plan")
public class PlanEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @NotBlank
    @Column(name = "username", nullable = false)
    private String email;

    @NotBlank
    @Column(name = "username", nullable = false)
    private int id_tarjeta;

    @NotBlank
    @Column(name = "username", nullable = false)
    private int id_expiracion;

    @NotBlank
    @Column(name = "username", nullable = false)
    private int id_CVV;
}
