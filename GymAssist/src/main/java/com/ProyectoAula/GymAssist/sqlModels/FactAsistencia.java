package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fact_asistencia")
public class FactAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cliente")
    private String idCliente;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "musculos")
    private String musculos;

    @Column(name = "id_gimnasio")
    private String idGimnasio;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMusculos() { return musculos; }
    public void setMusculos(String musculos) { this.musculos = musculos; }
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
}