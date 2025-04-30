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
  private Long gymId;

  @BsonProperty("fotosRutina")
  private List<FotoRutina> fotosRutina;

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

  public RutinaEntity gymId(Long gymId) {
    this.gymId = gymId;
    return this;
  }

  @JsonProperty("gymId")
  public Long getGymId() {
    return gymId;
  }

  public void setGymId(Long gymId) {
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
        Objects.equals(this.fotosRutina, rutina.fotosRutina);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, grupoMuscular, repeticiones, series, gymId, fotosRutina);
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
    private String url;
    private String descripcion;

    public FotoRutina() {}

    public FotoRutina(String url, String descripcion) {
      this.url = url;
      this.descripcion = descripcion;
    }

    @JsonProperty("url")
    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    @JsonProperty("descripcion")
    public String getDescripcion() {
      return descripcion;
    }

    public void setDescripcion(String descripcion) {
      this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      FotoRutina that = (FotoRutina) o;
      return Objects.equals(url, that.url) &&
             Objects.equals(descripcion, that.descripcion);
    }

    @Override
    public int hashCode() {
      return Objects.hash(url, descripcion);
    }

    @Override
    public String toString() {
      return "FotoRutina{" +
              "url='" + url + '\'' +
              ", descripcion='" + descripcion + '\'' +
              '}';
    }
  }
}