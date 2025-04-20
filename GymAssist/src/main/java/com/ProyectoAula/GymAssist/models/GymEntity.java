package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "gimnasios")
public class GymEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "nombre_gymnasio", nullable = false)
    private String nombre_gymnasio;

    @NotBlank
    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Min(1)
    @Column(name = "RUT", nullable = false)
    private int RUT;

    @Lob
    @Column(name = "fotos") // para MySQL; si usas otra BD, puede cambiar
    private byte[] fotos;

    @NotBlank
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @OneToOne
    @JoinColumn(name = "admin_id")
    private AdminEntity adminEntity;

    @OneToMany(mappedBy = "gimnasio")
    private List<ClientEntity> clientEntityList;

    @OneToMany(mappedBy = "gimnasio")
    private List<PlanEntity> planEntityList;
}
