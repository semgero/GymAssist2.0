package com.ProyectoAula.GymAssist.mongoModels;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Arrays;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;

@JsonTypeName("gimnasios")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:27:33.022640-05:00[America/Bogota]", comments = "Generator version: 7.10.0")@Document("gimnasios")
public class GimnasiosEntity {

  @Id
  @BsonProperty("_id")
  private ObjectId id = null;

  @BsonProperty("nit")
  private Integer nit;

  @BsonProperty("descripcion")
  private String descripcion;

  @BsonProperty("direccion")
  private String direccion;

  @BsonProperty("fotos")
  private byte[] fotos;

  @BsonProperty("nombreGymnasio")
  private String nombreGymnasio;

  @BsonProperty("adminId")
  private ObjectId adminId;

  public GimnasiosEntity id(ObjectId id) {
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

  public GimnasiosEntity nit(Integer nit) {
    this.nit = nit;
    return this;
  }

  /**
   * Get rut
   * @return rut
   */
  
  @JsonProperty("nit")
  public Integer getNit() {
    return nit;
  }

  public void setNit(Integer nit) {
    this.nit = nit;
  }

  public GimnasiosEntity descripcion(String descripcion) {
    this.descripcion = descripcion;
    return this;
  }

  /**
   * Get descripcion
   * @return descripcion
   */
  
  @JsonProperty("descripcion")
  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public GimnasiosEntity direccion(String direccion) {
    this.direccion = direccion;
    return this;
  }

  /**
   * Get direccion
   * @return direccion
   */
  
  @JsonProperty("direccion")
  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public GimnasiosEntity fotos(byte[] fotos) {
    this.fotos = fotos;
    return this;
  }

  /**
   * Get fotos
   * @return fotos
   */
  
  @JsonProperty("fotos")
  public byte[] getFotos() {
    return fotos;
  }

  public void setFotos(byte[] fotos) {
    this.fotos = fotos;
  }

  public GimnasiosEntity nombreGymnasio(String nombreGymnasio) {
    this.nombreGymnasio = nombreGymnasio;
    return this;
  }

  /**
   * Get nombreGymnasio
   * @return nombreGymnasio
   */
  
  @JsonProperty("nombreGymnasio")
  public String getNombreGymnasio() {
    return nombreGymnasio;
  }

  public void setNombreGymnasio(String nombreGymnasio) {
    this.nombreGymnasio = nombreGymnasio;
  }

  public GimnasiosEntity adminId(ObjectId adminId) {
    this.adminId = adminId;
    return this;
  }

  /**
   * Get adminId
   * @return adminId
   */
  
  @JsonProperty("adminId")
  public ObjectId getAdminId() {
    return adminId;
  }

  public void setAdminId(ObjectId adminId) {
    this.adminId = adminId;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GimnasiosEntity gimnasios = (GimnasiosEntity) o;
    return Objects.equals(this.id, gimnasios.id) &&
        Objects.equals(this.nit, gimnasios.nit) &&
        Objects.equals(this.descripcion, gimnasios.descripcion) &&
        Objects.equals(this.direccion, gimnasios.direccion) &&
        Arrays.equals(this.fotos, gimnasios.fotos) &&
        Objects.equals(this.nombreGymnasio, gimnasios.nombreGymnasio) &&
        Objects.equals(this.adminId, gimnasios.adminId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, nit, descripcion, direccion, Arrays.hashCode(fotos), nombreGymnasio, adminId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GimnasiosEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    nit: ").append(toIndentedString(nit)).append("\n");
    sb.append("    descripcion: ").append(toIndentedString(descripcion)).append("\n");
    sb.append("    direccion: ").append(toIndentedString(direccion)).append("\n");
    sb.append("    fotos: ").append(toIndentedString(fotos)).append("\n");
    sb.append("    nombreGymnasio: ").append(toIndentedString(nombreGymnasio)).append("\n");
    sb.append("    adminId: ").append(toIndentedString(adminId)).append("\n");
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