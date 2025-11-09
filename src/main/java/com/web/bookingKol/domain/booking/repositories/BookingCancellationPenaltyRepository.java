package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.domain.booking.models.BookingCancellationPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingCancellationPenaltyRepository extends JpaRepository<BookingCancellationPenalty, UUID> {
}

