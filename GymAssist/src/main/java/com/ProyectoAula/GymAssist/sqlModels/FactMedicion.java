package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fact_medicion")
public class FactMedicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente")
    private String idCliente;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "estatura")
    private Double estatura;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "id_gimnasio")
    private String idGimnasio;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public Double getEstatura() { return estatura; }
    public void setEstatura(Double estatura) { this.estatura = estatura; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
}