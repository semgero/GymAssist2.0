package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Admin")
@PrimaryKeyJoinColumn(name = "id")
public class AdminEntity extends UserEntity {

    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(mappedBy = "adminEntity", cascade = CascadeType.ALL)
    private GymEntity gymnasio;

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<RutinaEntity> rutinas;

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<ClientEntity> clientes;


}
