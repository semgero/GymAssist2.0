package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
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

    @OneToOne(mappedBy = "adminEntity", cascade = CascadeType.ALL)
    private GymEntity gimnasio;

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<RutinaEntity> rutinas;

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<ClientEntity> clientes;

}
