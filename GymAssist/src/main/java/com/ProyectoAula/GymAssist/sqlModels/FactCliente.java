package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fact_cliente")
public class FactCliente {

    @Id
    @Column(name = "id_cliente")
    private String idCliente;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "correo")
    private String correo;

    @Column(name = "id_documento")
    private String idDocumento;

    @Column(name = "telefono")
    private Integer telefono;

    @Column(name = "estado")
    private String estado;

    @Column(name = "subscription_status")
    private String subscriptionStatus;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_inicio_membresia")
    private LocalDate fechaInicioMembresia;

    @Column(name = "fecha_fin_membresia")
    private LocalDate fechaFinMembresia;

    @Column(name = "inasistencias")
    private Integer inasistencias;

    @Column(name = "id_plan")
    private String idPlan;

    @Column(name = "id_gimnasio")
    private String idGimnasio;

    // Getters y Setters
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getIdDocumento() { return idDocumento; }
    public void setIdDocumento(String idDocumento) { this.idDocumento = idDocumento; }
    public Integer getTelefono() { return telefono; }
    public void setTelefono(Integer telefono) { this.telefono = telefono; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaInicioMembresia() { return fechaInicioMembresia; }
    public void setFechaInicioMembresia(LocalDate fechaInicioMembresia) { this.fechaInicioMembresia = fechaInicioMembresia; }
    public LocalDate getFechaFinMembresia() { return fechaFinMembresia; }
    public void setFechaFinMembresia(LocalDate fechaFinMembresia) { this.fechaFinMembresia = fechaFinMembresia; }
    public Integer getInasistencias() { return inasistencias; }
    public void setInasistencias(Integer inasistencias) { this.inasistencias = inasistencias; }
    public String getIdPlan() { return idPlan; }
    public void setIdPlan(String idPlan) { this.idPlan = idPlan; }
    public String getIdGimnasio() { return idGimnasio; }
    public void setIdGimnasio(String idGimnasio) { this.idGimnasio = idGimnasio; }
}