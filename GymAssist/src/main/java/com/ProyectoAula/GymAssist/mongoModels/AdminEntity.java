package com.ProyectoAula.GymAssist.mongoModels;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonTypeName("admin")
@Document("admin")
public class AdminEntity {

    @BsonProperty("_id")
    private ObjectId mongoId = null;

    @BsonProperty("id")
    private Long id;

    @BsonProperty("username")
    private String username;

    @BsonProperty("email")
    private String email;

    @BsonProperty("password")
    private String password;

    @BsonProperty("role")
    private String role;

    // Getters y Setters
    public ObjectId getMongoId() {
        return mongoId;
    }

    public void setMongoId(ObjectId mongoId) {
        this.mongoId = mongoId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminEntity admin = (AdminEntity) o;
        return Objects.equals(this.mongoId, admin.mongoId) &&
                Objects.equals(this.id, admin.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mongoId, id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminEntity {\n");
        sb.append("    mongoId: ").append(toIndentedString(mongoId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
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