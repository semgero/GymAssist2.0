package com.ProyectoAula.GymAssist.mongoModels;
//
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.Id;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonTypeName("admin")
@Document("admin")
public class AdminEntity {

    @Id
    @BsonProperty("_id")
    private ObjectId id = null;

    @BsonProperty("userId")
    private ObjectId userId;

    // Getter y Setter solo para id
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
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
        return Objects.equals(this.id, admin.id)&&
                Objects.equals(this.userId, admin.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminEntity {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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