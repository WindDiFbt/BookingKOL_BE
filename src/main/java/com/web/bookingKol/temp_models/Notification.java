package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 100)
    @Column(name = "type", length = 100)
    private String type;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @ColumnDefault("false")
    @Column(name = "is_read")
    private Boolean isRead;

    @Size(max = 100)
    @Column(name = "related_type", length = 100)
    private String relatedType;

    @Column(name = "related_id")
    private UUID relatedId;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}