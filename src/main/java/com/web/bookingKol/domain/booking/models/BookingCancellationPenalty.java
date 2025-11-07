package com.web.bookingKol.domain.booking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "booking_cancellation_penalties")
public class BookingCancellationPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    @Column(name = "penalty_percent", precision = 5, scale = 2)
    private BigDecimal penaltyPercent;

    @Column(name = "penalty_amount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}

