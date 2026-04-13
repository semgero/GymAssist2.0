package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_gimnasio")
public class DimGimnasio {

    @Id
    @Column(name = "id_gimnasio")
    private String idGimnasio;

    @Column(name = "nit")
    private Integer nit;

    @Column(name = "nombre_gimnasio")
    private String nombreGimnasio;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "id_admin")
    private String idAdmin;

    // Getters y Setters
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
    public Integer getNit() { return nit; }
    public void setNit(Integer nit) { this.nit = nit; }
    public String getNombreGimnasio() { return nombreGimnasio; }
    public void setNombreGimnasio(String nombreGimnasio) { this.nombreGimnasio = nombreGimnasio; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getIdAdmin() { return idAdmin; }
    public void setIdAdmin(String idAdmin) { this.idAdmin = idAdmin; }
}