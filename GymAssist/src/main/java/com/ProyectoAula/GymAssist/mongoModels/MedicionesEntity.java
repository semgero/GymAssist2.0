package com.ProyectoAula.GymAssist.mongoModels;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDateTime;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.annotation.Generated;
import org.springframework.data.annotation.Id;

@JsonTypeName("mediciones")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-25T16:37:23.491826100-05:00[America/Bogota]", comments = "Generator version: 7.10.0")@Document("mediciones")
public class MedicionesEntity {

  @Id
  @BsonProperty("_id")
  private ObjectId id = null;

  @BsonProperty("estatura")
  private Double estatura;

  @BsonProperty("fechaRegistro")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fechaRegistro;

  @BsonProperty("peso")
  private Double peso;

  @BsonProperty("clienteId")
  private ObjectId  clienteId;

  public MedicionesEntity id(ObjectId id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @JsonProperty("_id")
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public MedicionesEntity estatura(Double estatura) {
    this.estatura = estatura;
    return this;
  }

  /**
   * Get estatura
   * @return estatura
   */
  
  @JsonProperty("estatura")
  public Double getEstatura() {
    return estatura;
  }

  public void setEstatura(Double estatura) {
    this.estatura = estatura;
  }

  public MedicionesEntity fechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
    return this;
  }

  /**
   * Get fechaRegistro
   * @return fechaRegistro
   */
  
  @JsonProperty("fechaRegistro")
  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  public MedicionesEntity peso(Double peso) {
    this.peso = peso;
    return this;
  }

  /**
   * Get peso
   * @return peso
   */
  
  @JsonProperty("peso")
  public Double getPeso() {
    return peso;
  }

  public void setPeso(Double peso) {
    this.peso = peso;
  }

  public MedicionesEntity clienteId(ObjectId  clienteId) {
    this.clienteId = clienteId;
    return this;
  }

  /**
   * Get clienteId
   * @return clienteId
   */
  
  @JsonProperty("clienteId")
  public ObjectId  getClienteId() {
    return clienteId;
  }

  public void setClienteId(ObjectId  clienteId) {
    this.clienteId = clienteId;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MedicionesEntity mediciones = (MedicionesEntity) o;
    return Objects.equals(this.id, mediciones.id) &&
        Objects.equals(this.estatura, mediciones.estatura) &&
        Objects.equals(this.fechaRegistro, mediciones.fechaRegistro) &&
        Objects.equals(this.peso, mediciones.peso) &&
        Objects.equals(this.clienteId, mediciones.clienteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, estatura, fechaRegistro, peso, clienteId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MedicionesEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    estatura: ").append(toIndentedString(estatura)).append("\n");
    sb.append("    fechaRegistro: ").append(toIndentedString(fechaRegistro)).append("\n");
    sb.append("    peso: ").append(toIndentedString(peso)).append("\n");
    sb.append("    clienteId: ").append(toIndentedString(clienteId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
