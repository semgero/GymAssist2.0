package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_plan")
public class DimPlan {

    @Id
    @Column(name = "id_plan")
    private String idPlan;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "id_gimnasio")
    private String idGimnasio;

    // Getters y Setters
    public String getIdPlan() { return idPlan; }
    public void setIdPlan(String idPlan) { this.idPlan = idPlan; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
}