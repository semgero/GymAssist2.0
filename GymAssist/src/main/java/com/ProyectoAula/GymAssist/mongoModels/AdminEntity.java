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

    // Getter y Setter solo para id
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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
        return Objects.equals(this.id, admin.id);
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