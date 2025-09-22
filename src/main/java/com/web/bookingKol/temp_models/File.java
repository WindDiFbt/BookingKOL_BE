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
@Table(name = "files")
public class File {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Size(max = 255)
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url", length = Integer.MAX_VALUE)
    private String fileUrl;

    @Size(max = 50)
    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Size(max = 100)
    @Column(name = "attached_type", length = 100)
    private String attachedType;

    @Column(name = "attached_id")
    private UUID attachedId;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}