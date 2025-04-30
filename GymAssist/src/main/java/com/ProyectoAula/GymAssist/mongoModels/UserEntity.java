package com.ProyectoAula.GymAssist.mongoModels;
//

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.Valid;
import jakarta.annotation.Generated;
import jakarta.persistence.Id;

@JsonTypeName("user")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:32:34.385757600-05:00[America/Bogota]", comments = "Generator version: 7.10.0")
@Document("user")
public class UserEntity {

    @Id
    @BsonProperty("_id")
    private ObjectId id; // Identificador principal de MongoDB

    @BsonProperty("email")
    private String email;

    @BsonProperty("password")
    private String password;

    @BsonProperty("role")
    private String role;

    @BsonProperty("username")
    private String username;

    @BsonProperty("userRoles")
    private List<@Valid UserUserRolesInnerEntity> userRoles = new ArrayList<>();

    // Getters y Setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<UserUserRolesInnerEntity> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserUserRolesInnerEntity> userRoles) {
        this.userRoles = userRoles;
    }

    public UserEntity addUserRolesItem(UserUserRolesInnerEntity userRolesItem) {
        if (this.userRoles == null) {
            this.userRoles = new ArrayList<>();
        }
        this.userRoles.add(userRolesItem);
        return this;
    }

    // Métodos de comparación y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserEntity user = (UserEntity) o;
        return Objects.equals(this.id, user.id) &&
               Objects.equals(this.email, user.email) &&
               Objects.equals(this.password, user.password) &&
               Objects.equals(this.role, user.role) &&
               Objects.equals(this.username, user.username) &&
               Objects.equals(this.userRoles, user.userRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, role, username, userRoles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserEntity {\n")
          .append("    id: ").append(toIndentedString(id)).append("\n")
          .append("    email: ").append(toIndentedString(email)).append("\n")
          .append("    password: ").append(toIndentedString(password)).append("\n")
          .append("    role: ").append(toIndentedString(role)).append("\n")
          .append("    username: ").append(toIndentedString(username)).append("\n")
          .append("    userRoles: ").append(toIndentedString(userRoles)).append("\n")
          .append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}