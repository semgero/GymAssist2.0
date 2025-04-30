package com.ProyectoAula.GymAssist.mongoModels;
//

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;

@JsonTypeName("user_userRoles_inner_role")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:32:34.385757600-05:00[America/Bogota]", comments = "Generator version: 7.10.0")@Document("user_userRoles_inner_role")
public class UserUserRolesInnerRoleEntity {

  @Id
  @BsonProperty("id")
  private Long id;

  @BsonProperty("name")
  private String name;

  public UserUserRolesInnerRoleEntity id(Long id) {
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

  public UserUserRolesInnerRoleEntity name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserUserRolesInnerRoleEntity userUserRolesInnerRole = (UserUserRolesInnerRoleEntity) o;
    return Objects.equals(this.id, userUserRolesInnerRole.id) &&
        Objects.equals(this.name, userUserRolesInnerRole.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserUserRolesInnerRoleEntity {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
