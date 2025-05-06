package com.ProyectoAula.GymAssist.mongoModels;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;

@JsonTypeName("plan")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:29:40.526823800-05:00[America/Bogota]", comments = "Generator version: 7.10.0")
@Document("plan")
public class PlanEntity {

  @Id
  @BsonProperty("_id")
  private ObjectId id = null;

  @BsonProperty("nombre")
  private String nombre;

  @BsonProperty("descripcion")
  private String descripcion;

  @BsonProperty("precio")
  private Double precio;

  @BsonProperty("duracion")
  private Integer duracion;

  @BsonProperty("beneficios")
  private List<String> beneficios;

  @BsonProperty("gymId")
  private ObjectId gymId;

  @BsonProperty("clienteId")
  private Long clienteId;

  public PlanEntity id(ObjectId id) {
    this.id = id;
    return this;
  }

  @JsonProperty("_id")
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public PlanEntity nombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  @JsonProperty("nombre")
  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public PlanEntity descripcion(String descripcion) {
    this.descripcion = descripcion;
    return this;
  }

  @JsonProperty("descripcion")
  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public PlanEntity precio(Double precio) {
    this.precio = precio;
    return this;
  }

  @JsonProperty("precio")
  public Double getPrecio() {
    return precio;
  }

  public void setPrecio(Double precio) {
    this.precio = precio;
  }

  public PlanEntity duracion(Integer duracion) {
    this.duracion = duracion;
    return this;
  }

  @JsonProperty("duracion")
  public Integer getDuracion() {
    return duracion;
  }

  public void setDuracion(Integer duracion) {
    this.duracion = duracion;
  }

  public PlanEntity beneficios(List<String> beneficios) {
    this.beneficios = beneficios;
    return this;
  }

  @JsonProperty("beneficios")
  public List<String> getBeneficios() {
    return beneficios;
  }

  public void setBeneficios(List<String> beneficios) {
    this.beneficios = beneficios;
  }

  public PlanEntity gymId(ObjectId gymId) {
    this.gymId = gymId;
    return this;
  }

  @JsonProperty("gymId")
  public ObjectId gymId() {
    return gymId;
  }

  public ObjectId getGymId() {
    return gymId;
  }

  public void setGymId(ObjectId gymId) {
    this.gymId = gymId;
  }

  public PlanEntity clienteId(Long clienteId) {
    this.clienteId = clienteId;
    return this;
  }

  @JsonProperty("clienteId")
  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long clienteId) {
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
    PlanEntity plan = (PlanEntity) o;
    return Objects.equals(this.id, plan.id) &&
        Objects.equals(this.nombre, plan.nombre) &&
        Objects.equals(this.descripcion, plan.descripcion) &&
        Objects.equals(this.precio, plan.precio) &&
        Objects.equals(this.duracion, plan.duracion) &&
        Objects.equals(this.beneficios, plan.beneficios) &&
        Objects.equals(this.gymId, plan.gymId) &&
        Objects.equals(this.clienteId, plan.clienteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, nombre, descripcion, precio, duracion, beneficios, gymId, clienteId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlanEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    nombre: ").append(toIndentedString(nombre)).append("\n");
    sb.append("    descripcion: ").append(toIndentedString(descripcion)).append("\n");
    sb.append("    precio: ").append(toIndentedString(precio)).append("\n");
    sb.append("    duracion: ").append(toIndentedString(duracion)).append("\n");
    sb.append("    beneficios: ").append(toIndentedString(beneficios)).append("\n");
    sb.append("    gimnasioId: ").append(toIndentedString(gymId)).append("\n");
    sb.append("    clienteId: ").append(toIndentedString(clienteId)).append("\n");
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