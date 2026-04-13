package com.ProyectoAula.GymAssist.sqlModels;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_admin")
public class DimAdmin {

    @Id
    @Column(name = "id_admin")
    private String idAdmin;

    @Column(name = "id_user")
    private String idUser;

    @Column(name = "email", columnDefinition = "TEXT")
    private String email;

    @Column(name = "username", columnDefinition = "TEXT")
    private String username;

    // Getters y Setters
    public String getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(String idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}