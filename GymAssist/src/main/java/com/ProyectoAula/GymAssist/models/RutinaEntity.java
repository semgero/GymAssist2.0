package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
    @Column(columnDefinition = "fotos") // para MySQL; si usas otra BD, puede cambiar
    private byte[] fotos;

    @NotBlank
    @Column(name = "series", nullable = false)
    private String series;

    @NotBlank
    @Column(name = "repeticiones", nullable = false)
    private String repeticiones;
}

