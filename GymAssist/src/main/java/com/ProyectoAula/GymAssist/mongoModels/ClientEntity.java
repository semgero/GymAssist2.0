package com.ProyectoAula.GymAssist.mongoModels;
//

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;


@JsonTypeName("client")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-25T23:05:17.426110800-05:00[America/Bogota]", comments = "Generator version: 7.10.0")
@Document("client")
public class ClientEntity {

  @Id
  @BsonProperty("_id")
  private ObjectId id = null;

  @BsonProperty("activo")
  private Boolean activo = true;

  @BsonProperty("correo")
  private String correo;

  @BsonProperty("idDocumento")
  private String idDocumento;

  @BsonProperty("mensualidad")
  private String mensualidad;

  @BsonProperty("nombre")
  private String nombre;

  @BsonProperty("telefono")
  private Integer telefono;

  @BsonProperty("adminId")
  private ObjectId adminId;

  @BsonProperty("gymId")
  private ObjectId gymId;

  @BsonProperty("username")
  private String username;

  @BsonProperty("password")
  private String password;

  @BsonProperty("asistencias")
    private List<Asistencia> asistencias = new ArrayList<>();

    @BsonProperty("inasistencias")
    private int inasistencias = 0;

  // --- Getters and Setters ---

  public ObjectId getId() {
    return id;
}

public void setId(ObjectId id) {
    this.id = id;
}

public Boolean getActivo() {
    return activo;
}

public void setActivo(Boolean activo) {
    this.activo = activo;
}

public String getCorreo() {
    return correo;
}

public void setCorreo(String correo) {
    this.correo = correo;
}

public String getIdDocumento() {
    return idDocumento;
}

public void setIdDocumento(String idDocumento) {
    this.idDocumento = idDocumento;
}

public String getMensualidad() {
    return mensualidad;
}

public void setMensualidad(String mensualidad) {
    this.mensualidad = mensualidad;
}

public String getNombre() {
    return nombre;
}

public void setNombre(String nombre) {
    this.nombre = nombre;
}

public Integer getTelefono() {
    return telefono;
}

public void setTelefono(Integer telefono) {
    this.telefono = telefono;
}

public ObjectId getAdminId() {
    return adminId;
}

public void setAdminId(ObjectId adminId) {
    this.adminId = adminId;
}

public ObjectId getGymId() {
    return gymId;
}

public void setGymId(ObjectId gymId) {
    this.gymId = gymId;
}

public String getUsername() {
    return username;
}

public void setUsername(String username) {
    this.username = username;
}

public String getPassword() {
    return password;
}

public void setPassword(String password) {
    this.password = password;
}

public List<Asistencia> getAsistencias() {
    return asistencias;
}

public void setAsistencias(List<Asistencia> asistencias) {
    this.asistencias = asistencias;
}

public int getInasistencias() {
    return inasistencias;
}

public void setInasistencias(int inasistencias) {
    this.inasistencias = inasistencias;
}

// ----------- Clase interna para asistencia -----------

public static class Asistencia {
    private LocalDate fecha;
    private List<String> musculos;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public List<String> getMusculos() {
        return musculos;
    }

    public void setMusculos(List<String> musculos) {
        this.musculos = musculos;
    }
}

// ----------- Equals, HashCode y ToString ------------

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClientEntity)) return false;
    ClientEntity that = (ClientEntity) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(correo, that.correo) &&
            Objects.equals(username, that.username);
}

@Override
public int hashCode() {
    return Objects.hash(id, correo, username);
}

@Override
public String toString() {
    return "ClientEntity{" +
            "id=" + id +
            ", nombre='" + nombre + '\'' +
            ", correo='" + correo + '\'' +
            ", username='" + username + '\'' +
            '}';
}
}

