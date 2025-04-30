package com.ProyectoAula.GymAssist.mongoModels;
//

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.annotation.Generated;

@JsonTypeName("user_userRoles_inner")
@Generated(value = "com.mongodb.migrator.application.codegen.config.java.JavaSpringCodegenConfig", date = "2025-04-23T20:32:34.385757600-05:00[America/Bogota]", comments = "Generator version: 7.10.0")@Document("user_userRoles_inner")
public class UserUserRolesInnerEntity {

    @BsonProperty("userId")
    private Long userId;

    @BsonProperty("roleId")
    private Long roleId;

    @BsonProperty("role")
    private UserUserRolesInnerRoleEntity role;

    public UserUserRolesInnerEntity userId(Long userId) {
      this.userId = userId;
      return this;
    }

    /**
     * Get userId
     * @return userId
     */
    @JsonProperty("userId")
    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public UserUserRolesInnerEntity roleId(Long roleId) {
      this.roleId = roleId;
      return this;
    }

    /**
     * Get roleId
     * @return roleId
     */
    @JsonProperty("roleId")
    public Long getRoleId() {
      return roleId;
    }

    public void setRoleId(Long roleId) {
      this.roleId = roleId;
    }

    public UserUserRolesInnerEntity role(UserUserRolesInnerRoleEntity role) {
      this.role = role;
      return this;
    }

    /**
     * Get role
     * @return role
     */
    @JsonProperty("role")
    public UserUserRolesInnerRoleEntity getRole() {
      return role;
    }

    public void setRole(UserUserRolesInnerRoleEntity role) {
      this.role = role;
    }

    /**
     * Get role name safely
     * @return role name or null if role is not set
     */
    public String getRoleName() {
      return (this.role != null) ? this.role.getName() : null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      UserUserRolesInnerEntity userUserRolesInner = (UserUserRolesInnerEntity) o;
      return Objects.equals(this.userId, userUserRolesInner.userId) &&
            Objects.equals(this.roleId, userUserRolesInner.roleId) &&
            Objects.equals(this.role, userUserRolesInner.role);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, roleId, role);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class UserUserRolesInnerEntity {\n");
      sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
      sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
      sb.append("    role: ").append(toIndentedString(role)).append("\n");
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