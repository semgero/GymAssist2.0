package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_rutina")
public class DimRutina {

    @Id
    @Column(name = "id_rutina")
    private String idRutina;

    @Column(name = "nombre_ejercicio")
    private String nombreEjercicio;

    @Column(name = "grupo_muscular")
    private String grupoMuscular;

    @Column(name = "repeticiones")
    private String repeticiones;

    @Column(name = "series")
    private String series;

    @Column(name = "id_gimnasio")
    private String idGimnasio;

    // Getters y Setters
    public String getIdRutina() { return idRutina; }
    public void setIdRutina(String idRutina) { this.idRutina = idRutina; }
    public String getNombreEjercicio() { return nombreEjercicio; }
    public void setNombreEjercicio(String nombreEjercicio) { this.nombreEjercicio = nombreEjercicio; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public String getRepeticiones() { return repeticiones; }
    public void setRepeticiones(String repeticiones) { this.repeticiones = repeticiones; }
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
}