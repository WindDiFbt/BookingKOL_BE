package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kol_availabilities")
public class KolAvailability {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "google_event_id")
    private String googleEventId;


    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "note", length = Integer.MAX_VALUE)
    private String note;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}