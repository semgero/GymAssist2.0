package com.ProyectoAula.GymAssist.mongoModels;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import jakarta.annotation.Generated;
import java.time.LocalDateTime;

@JsonTypeName("rutina")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:31:20.848102300-05:00[America/Bogota]", comments = "Generator version: 7.10.0")
@Document("rutina")
public class RutinaEntity {

  @Id
  @BsonProperty("_id")
  private ObjectId id = null;

  @BsonProperty("nombreEjercicio")
  private String nombreEjercicio;

  @BsonProperty("grupoMuscular")
  private String grupoMuscular;

  @BsonProperty("repeticiones")
  private String repeticiones;

  @BsonProperty("series")
  private String series;

  @BsonProperty("gymId")
  private ObjectId gymId;

  @BsonProperty("fotosRutina")
  private List<FotoRutina> fotosRutina;

  @BsonProperty("createdAt")
  private LocalDateTime createdAt;

  @BsonProperty("updatedAt")
  private LocalDateTime updatedAt;

  public RutinaEntity nombreEjercicio(String nombreEjercicio) {
    this.nombreEjercicio = nombreEjercicio;
    return this;
  }

  public String getNombreEjercicio() {
    return nombreEjercicio;
  }

  public void setNombreEjercicio(String nombreEjercicio) {
    this.nombreEjercicio = nombreEjercicio;
  }

  public RutinaEntity createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public RutinaEntity updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public RutinaEntity id(ObjectId id) {
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

  public RutinaEntity grupoMuscular(String grupoMuscular) {
    this.grupoMuscular = grupoMuscular;
    return this;
  }

  @JsonProperty("grupoMuscular")
  public String getGrupoMuscular() {
    return grupoMuscular;
  }

  public void setGrupoMuscular(String grupoMuscular) {
    this.grupoMuscular = grupoMuscular;
  }

  public RutinaEntity repeticiones(String repeticiones) {
    this.repeticiones = repeticiones;
    return this;
  }

  @JsonProperty("repeticiones")
  public String getRepeticiones() {
    return repeticiones;
  }

  public void setRepeticiones(String repeticiones) {
    this.repeticiones = repeticiones;
  }

  public RutinaEntity series(String series) {
    this.series = series;
    return this;
  }

  @JsonProperty("series")
  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public RutinaEntity gymId(ObjectId gymId) {
    this.gymId = gymId;
    return this;
  }

  @JsonProperty("gymId")
  public ObjectId getGymId() {
    return gymId;
  }

  public void setGymId(ObjectId gymId) {
    this.gymId = gymId;
  }

  public RutinaEntity fotosRutina(List<FotoRutina> fotosRutina) {
    this.fotosRutina = fotosRutina;
    return this;
  }

  @JsonProperty("fotosRutina")
  public List<FotoRutina> getFotosRutina() {
    return fotosRutina;
  }

  public void setFotosRutina(List<FotoRutina> fotosRutina) {
    this.fotosRutina = fotosRutina;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RutinaEntity rutina = (RutinaEntity) o;
    return Objects.equals(this.id, rutina.id) &&
        Objects.equals(this.grupoMuscular, rutina.grupoMuscular) &&
        Objects.equals(this.nombreEjercicio, rutina.nombreEjercicio) &&
        Objects.equals(this.repeticiones, rutina.repeticiones) &&
        Objects.equals(this.series, rutina.series) &&
        Objects.equals(this.gymId, rutina.gymId) &&
        Objects.equals(this.fotosRutina, rutina.fotosRutina) &&
        Objects.equals(this.createdAt, rutina.createdAt) &&
        Objects.equals(this.updatedAt, rutina.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, grupoMuscular, nombreEjercicio, repeticiones, series, gymId, fotosRutina, createdAt,
        updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RutinaEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    grupoMuscular: ").append(toIndentedString(grupoMuscular)).append("\n");
    sb.append("    nombreEjercicio: ").append(toIndentedString(nombreEjercicio)).append("\n");
    sb.append("    repeticiones: ").append(toIndentedString(repeticiones)).append("\n");
    sb.append("    series: ").append(toIndentedString(series)).append("\n");
    sb.append("    gymId: ").append(toIndentedString(gymId)).append("\n");
    sb.append("    fotosRutina: ").append(toIndentedString(fotosRutina)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public static class FotoRutina {
    private String nombreArchivo;
    private String descripcion;
    private String rutaImagen;
    private String contentType;

    public FotoRutina(String nombreArchivo, String descripcion,
        String rutaImagen, String contentType) {
      this.nombreArchivo = nombreArchivo;
      this.descripcion = descripcion;
      this.rutaImagen = rutaImagen;
      this.contentType = contentType;
    }

    @JsonProperty("nombreArchivo")
    public String getNombreArchivo() {
      return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
      this.nombreArchivo = nombreArchivo;
    }

    @JsonProperty("descripcion")
    public String getDescripcion() {
      return descripcion;
    }

    public void setDescripcion(String descripcion) {
      this.descripcion = descripcion;
    }

    @JsonProperty("rutaImagen")
    public String getRutaImagen() {
      return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
      this.rutaImagen = rutaImagen;
    }

    @JsonProperty("contentType")
    public String getContentType() {
      return contentType;
    }

    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      FotoRutina that = (FotoRutina) o;
      return Objects.equals(nombreArchivo, that.nombreArchivo) &&
          Objects.equals(descripcion, that.descripcion) &&
          Objects.equals(rutaImagen, that.rutaImagen) &&
          Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nombreArchivo, descripcion, rutaImagen, contentType);
    }

    @Override
    public String toString() {
      return "FotoRutina{" +
          ", nombreArchivo='" + nombreArchivo + '\'' +
          ", descripcion='" + descripcion + '\'' +
          ", rutaImagen='" + rutaImagen + '\'' +
          ", contentType='" + contentType + '\'' +
          '}';
    }
  }
}