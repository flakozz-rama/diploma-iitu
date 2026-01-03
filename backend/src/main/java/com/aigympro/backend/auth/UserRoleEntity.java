package com.aigympro.backend.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRoleEntity {

    @Setter
    @Getter
    @Id
    @Column(name="user_id")
    private UUID userId;

    @Setter
    @Getter
    @Id
    @Column(name="role_id")
    private UUID roleId;

    @Column(name="granted_at", nullable=false)
    private OffsetDateTime grantedAt = OffsetDateTime.now();

}

class UserRoleId implements Serializable {
    private UUID userId;
    private UUID roleId;
    public UserRoleId() {}
    public UserRoleId(UUID u, UUID r){this.userId=u;this.roleId=r;}
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof UserRoleId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }
    @Override public int hashCode(){ return Objects.hash(userId, roleId); }
}
