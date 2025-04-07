package com.ProyectoAula.GymAssist.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @NotBlank
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "id_tarjeta", nullable = false)
    private int id_tarjeta;

    @Column(name = "id_expiracion", nullable = false)
    private int id_expiracion;

    @Column(name = "id_CVV", nullable = false)
    private int id_CVV;

    // Relación con gimnasio (Muchos planes pueden pertenecer a un gimnasio)
    @ManyToOne
    @JoinColumn(name = "gymnasio_id")
    private GymEntity gymnasio;

    // Relación 1:1 con cliente (Un plan es pagado por un cliente)
    @OneToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id")
    private ClientEntity cliente;
}
