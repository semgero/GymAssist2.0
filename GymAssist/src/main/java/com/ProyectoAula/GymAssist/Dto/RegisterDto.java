package com.ProyectoAula.GymAssist.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDto {
    
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String role; // CLIENTE o ADMIN

    // Solo para cliente
    private String nombre;
    private String mensualidad;
    private String id_Documento;
    private Integer telefono;
    private String correo;
}
