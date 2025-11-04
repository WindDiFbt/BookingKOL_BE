package com.web.bookingKol.domain.booking.repositories;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRequestParticipantRepository extends JpaRepository<BookingRequestParticipant, UUID> {
    List<BookingRequestParticipant> findByBookingRequest_Id(UUID bookingRequestId);
    boolean existsByBookingRequest_IdAndKol_IdAndRole(UUID bookingRequestId, UUID kolId, Enums.BookingParticipantRole role);
    void deleteByBookingRequest_IdAndKol_IdAndRole(UUID bookingRequestId, UUID kolId, Enums.BookingParticipantRole role);
}

