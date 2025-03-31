package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
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

    private String numeroDocumento;
    private String telefono;
}
