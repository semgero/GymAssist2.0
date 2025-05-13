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
        Objects.equals(this.repeticiones, rutina.repeticiones) &&
        Objects.equals(this.series, rutina.series) &&
        Objects.equals(this.gymId, rutina.gymId) &&
        Objects.equals(this.fotosRutina, rutina.fotosRutina) &&
        Objects.equals(this.createdAt, rutina.createdAt) &&
        Objects.equals(this.updatedAt, rutina.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, grupoMuscular, repeticiones, series, gymId, fotosRutina, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RutinaEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    grupoMuscular: ").append(toIndentedString(grupoMuscular)).append("\n");
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

  // Clase interna para fotos y descripciones
  public static class FotoRutina {
    // Campos
    private String nombreEjercicio; // ✅ Atributo añadido aquí
    private String nombreArchivo;
    private String descripcion;
    private String imagenBase64; // Imagen en Base64
    private String contentType; // Tipo MIME (ej: "image/jpeg")

    // Constructor completo
    public FotoRutina(String nombreEjercicio, String nombreArchivo, String descripcion,
        String imagenBase64, String contentType) {
      this.nombreEjercicio = nombreEjercicio;
      this.nombreArchivo = nombreArchivo;
      this.descripcion = descripcion;
      this.imagenBase64 = imagenBase64;
      this.contentType = contentType;
    }

    // Getters y Setters (¡OBLIGATORIOS para Spring Data MongoDB!)
    @JsonProperty("nombreEjercicio")
    public String getNombreEjercicio() {
      return nombreEjercicio;
    }

    public void setNombreEjercicio(String nombreEjercicio) {
      this.nombreEjercicio = nombreEjercicio;
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

    @JsonProperty("imagenBase64")
    public String getImagenBase64() {
      return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
      this.imagenBase64 = imagenBase64;
    }

    @JsonProperty("contentType")
    public String getContentType() {
      return contentType;
    }

    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    // equals(), hashCode() y toString() actualizados
    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      FotoRutina that = (FotoRutina) o;
      return Objects.equals(nombreEjercicio, that.nombreEjercicio) &&
          Objects.equals(nombreArchivo, that.nombreArchivo) &&
          Objects.equals(descripcion, that.descripcion) &&
          Objects.equals(imagenBase64, that.imagenBase64) &&
          Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nombreEjercicio, nombreArchivo, descripcion, imagenBase64, contentType);
    }

    @Override
    public String toString() {
      return "FotoRutina{" +
          "nombreEjercicio='" + nombreEjercicio + '\'' +
          ", nombreArchivo='" + nombreArchivo + '\'' +
          ", descripcion='" + descripcion + '\'' +
          ", contentType='" + contentType + '\'' +
          '}'; // Nota: No mostramos imagenBase64 (es muy largo)
    }
  }
}