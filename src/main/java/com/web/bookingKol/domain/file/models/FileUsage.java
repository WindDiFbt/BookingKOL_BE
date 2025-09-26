package com.web.bookingKol.domain.file.models;

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
@Table(name = "file_usages")
public class FileUsage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Size(max = 100)
    @Column(name = "target_type", length = 100)
    private String targetType;

    @ColumnDefault("false")
    @Column(name = "is_cover")
    private Boolean isCover;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}