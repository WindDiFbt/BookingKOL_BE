package com.web.bookingKol.domain.kol.repositories;

import com.web.bookingKol.domain.kol.models.GoogleCalendarLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoogleCalendarLinkRepository extends JpaRepository<GoogleCalendarLink, UUID> {
    Optional<GoogleCalendarLink> findByUserId(UUID userId);
}

