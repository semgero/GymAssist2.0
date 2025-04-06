package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Client")
@PrimaryKeyJoinColumn(name = "id")
public class ClientEntity extends UserEntity {

    @NotBlank
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "mensualidad", nullable = false)
    private String mensualidad;

    @NotBlank
    @Column(name = "id_Documento", nullable = false)
    private String id_Documento;

    @NotBlank
    @Column(name = "telefono", nullable = false)
    private int telefono;

    @Email
    @NotBlank
    @Column(name = "correo", nullable = false)
    private String correo;
}
