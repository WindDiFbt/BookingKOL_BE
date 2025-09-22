package com.web.bookingKol.domain.user.models;

import com.web.bookingKol.temp_models.Permission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "role_permissions")
public class RolePermission {
    @EmbeddedId
    private RolePermissionId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}

@Embeddable
@Data
class RolePermissionId implements Serializable {
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionId that)) return false;
        return roleId.equals(that.roleId) && permissionId.equals(that.permissionId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(roleId, permissionId);
    }
}