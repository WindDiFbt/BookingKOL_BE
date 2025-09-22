package com.web.bookingKol.domain.user.models;

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
@Table(name = "user_roles")
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;
}

@Embeddable
@Data
class UserRoleId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_id")
    private Integer roleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId that)) return false;
        return userId.equals(that.userId) && roleId.equals(that.roleId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, roleId);
    }
}