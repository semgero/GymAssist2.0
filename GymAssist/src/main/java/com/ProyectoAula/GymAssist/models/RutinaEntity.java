package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rutina")
public class RutinaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    @Column(name = "grupo_muscular", nullable = false)
    private String grupo_muscular;

    @Lob
    @Column(name = "fotos") // para MySQL; si usas otra BD, puede cambiar
    private byte[] fotos;

    @NotBlank
    @Column(name = "series", nullable = false)
    private String series;

    @NotBlank
    @Column(name = "repeticiones", nullable = false)
    private String repeticiones;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    private GymEntity gimnasio;
}

