package com.ProyectoAula.GymAssist.mongoModels;

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
  private ObjectId mongoId = null;

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
  private Long adminId;

  @BsonProperty("gymId")
  private Long gymId;

  @BsonProperty("username")
  private String username;

  @BsonProperty("password")
  private String password;

  // --- Getters and Setters ---

  public ObjectId getMongoId() {
    return mongoId;
  }

  public void setMongoId(ObjectId mongoId) {
    this.mongoId = mongoId;
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

  public Long getAdminId() {
    return adminId;
  }

  public void setAdminId(Long adminId) {
    this.adminId = adminId;
  }

  public Long getGymId() {
    return gymId;
  }

  public void setGymId(Long gymId) {
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

  // --- Equals and HashCode ---

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClientEntity client = (ClientEntity) o;
    return Objects.equals(mongoId, client.mongoId) &&
        Objects.equals(correo, client.correo) &&
        Objects.equals(idDocumento, client.idDocumento) &&
        Objects.equals(mensualidad, client.mensualidad) &&
        Objects.equals(nombre, client.nombre) &&
        Objects.equals(telefono, client.telefono) &&
        Objects.equals(adminId, client.adminId) &&
        Objects.equals(gymId, client.gymId) &&
        Objects.equals(username, client.username) &&
        Objects.equals(password, client.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mongoId, correo, idDocumento, mensualidad, nombre, telefono, adminId, gymId, username, password);
  }

  // --- ToString ---

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientEntity {\n");
    sb.append("    mongoId: ").append(toIndentedString(mongoId)).append("\n");
    sb.append("    correo: ").append(toIndentedString(correo)).append("\n");
    sb.append("    idDocumento: ").append(toIndentedString(idDocumento)).append("\n");
    sb.append("    mensualidad: ").append(toIndentedString(mensualidad)).append("\n");
    sb.append("    nombre: ").append(toIndentedString(nombre)).append("\n");
    sb.append("    telefono: ").append(toIndentedString(telefono)).append("\n");
    sb.append("    adminId: ").append(toIndentedString(adminId)).append("\n");
    sb.append("    gymId: ").append(toIndentedString(gymId)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

