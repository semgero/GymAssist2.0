package com.ProyectoAula.GymAssist.mongoModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "mediciones")
public class Medicion {

    @Id
    private String id;

    private Long clienteId; // Aquí haces la relación con la tabla relacional

    private double estatura; // en metros
    private double peso;     // en kg

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Medicion() {
    }

    public Medicion(String id, Long clienteId, double estatura, double peso, LocalDateTime fechaRegistro) {
        this.id = id;
        this.clienteId = clienteId;
        this.estatura = estatura;
        this.peso = peso;
        this.fechaRegistro = fechaRegistro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public double getEstatura() {
        return estatura;
    }

    public void setEstatura(double estatura) {
        this.estatura = estatura;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
