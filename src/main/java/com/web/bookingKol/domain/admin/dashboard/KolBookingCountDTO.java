package com.web.bookingKol.domain.admin.dashboard;

import java.util.UUID;

public interface KolBookingCountDTO {
    UUID getKolId();

    String getKolName();

    Long getBookingCount();
}
