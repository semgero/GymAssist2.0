package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "gimnasios")
public class GymEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Email
    @NotBlank
    @Column(name = "nombre_gimnasio", nullable = false)
    private String nombre_gimnasio;

    @NotBlank
    @Column(name = "direccion", nullable = false)
    private String direccion;

    @NotBlank
    @Column(name = "RUT", nullable = false)
    private int RUT;

    @Lob
    @Column(columnDefinition = "fotos") // para MySQL; si usas otra BD, puede cambiar
    private byte[] fotos;

    @NotBlank
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

}
