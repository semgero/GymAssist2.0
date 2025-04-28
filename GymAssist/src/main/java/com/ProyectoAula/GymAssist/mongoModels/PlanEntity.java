package com.ProyectoAula.GymAssist.mongoModels;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;

@JsonTypeName("plan")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:29:40.526823800-05:00[America/Bogota]", comments = "Generator version: 7.10.0")@Document("plan")
public class PlanEntity {

  @BsonProperty("_id")
  private ObjectId mongoId = null;

  @Id
  @BsonProperty("id")
  private Long id;

  @BsonProperty("email")
  private String email;

  @BsonProperty("idCvv")
  private Integer idCvv;

  @BsonProperty("idExpiracion")
  private Integer idExpiracion;

  @BsonProperty("idTarjeta")
  private Integer idTarjeta;

  @BsonProperty("username")
  private String username;

  @BsonProperty("clienteId")
  private Long clienteId;

  @BsonProperty("gimnasioId")
  private Long gimnasioId;

  public PlanEntity mongoId(ObjectId mongoId) {
    this.mongoId = mongoId;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @JsonProperty("_id")
  public ObjectId getMongoId() {
    return mongoId;
  }

  public void setMongoId(ObjectId mongoId) {
    this.mongoId = mongoId;
  }

  public PlanEntity id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PlanEntity email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public PlanEntity idCvv(Integer idCvv) {
    this.idCvv = idCvv;
    return this;
  }

  /**
   * Get idCvv
   * @return idCvv
   */
  
  @JsonProperty("idCvv")
  public Integer getIdCvv() {
    return idCvv;
  }

  public void setIdCvv(Integer idCvv) {
    this.idCvv = idCvv;
  }

  public PlanEntity idExpiracion(Integer idExpiracion) {
    this.idExpiracion = idExpiracion;
    return this;
  }

  /**
   * Get idExpiracion
   * @return idExpiracion
   */
  
  @JsonProperty("idExpiracion")
  public Integer getIdExpiracion() {
    return idExpiracion;
  }

  public void setIdExpiracion(Integer idExpiracion) {
    this.idExpiracion = idExpiracion;
  }

  public PlanEntity idTarjeta(Integer idTarjeta) {
    this.idTarjeta = idTarjeta;
    return this;
  }

  /**
   * Get idTarjeta
   * @return idTarjeta
   */
  
  @JsonProperty("idTarjeta")
  public Integer getIdTarjeta() {
    return idTarjeta;
  }

  public void setIdTarjeta(Integer idTarjeta) {
    this.idTarjeta = idTarjeta;
  }

  public PlanEntity username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
   */
  
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public PlanEntity clienteId(Long clienteId) {
    this.clienteId = clienteId;
    return this;
  }

  /**
   * Get clienteId
   * @return clienteId
   */
  
  @JsonProperty("clienteId")
  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
    this.clienteId = clienteId;
  }

  public PlanEntity gimnasioId(Long gimnasioId) {
    this.gimnasioId = gimnasioId;
    return this;
  }

  /**
   * Get gimnasioId
   * @return gimnasioId
   */
  
  @JsonProperty("gimnasioId")
  public Long getGimnasioId() {
    return gimnasioId;
  }

  public void setGimnasioId(Long gimnasioId) {
    this.gimnasioId = gimnasioId;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlanEntity plan = (PlanEntity) o;
    return Objects.equals(this.mongoId, plan.mongoId) &&
        Objects.equals(this.id, plan.id) &&
        Objects.equals(this.email, plan.email) &&
        Objects.equals(this.idCvv, plan.idCvv) &&
        Objects.equals(this.idExpiracion, plan.idExpiracion) &&
        Objects.equals(this.idTarjeta, plan.idTarjeta) &&
        Objects.equals(this.username, plan.username) &&
        Objects.equals(this.clienteId, plan.clienteId) &&
        Objects.equals(this.gimnasioId, plan.gimnasioId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mongoId, id, email, idCvv, idExpiracion, idTarjeta, username, clienteId, gimnasioId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlanEntity {\n");
    sb.append("    mongoId: ").append(toIndentedString(mongoId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    idCvv: ").append(toIndentedString(idCvv)).append("\n");
    sb.append("    idExpiracion: ").append(toIndentedString(idExpiracion)).append("\n");
    sb.append("    idTarjeta: ").append(toIndentedString(idTarjeta)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    clienteId: ").append(toIndentedString(clienteId)).append("\n");
    sb.append("    gimnasioId: ").append(toIndentedString(gimnasioId)).append("\n");
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