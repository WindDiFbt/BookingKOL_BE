package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kol_work_times")
public class KolWorkTime {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "availability_id", nullable = false)
    private KolAvailability availability;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "booking_request_id", nullable = true)
    private BookingRequest bookingRequest;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", length = 50)
    private String status;

}