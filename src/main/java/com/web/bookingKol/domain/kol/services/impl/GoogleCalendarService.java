package com.web.bookingKol.domain.kol.services.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.repositories.GoogleCalendarLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;



import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarLinkRepository linkRepo;

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.client-secret}")
    private String clientSecret;
    private Calendar buildClient(String refreshToken) throws IOException, GeneralSecurityException {
        var creds = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(creds))
                .setApplicationName("BookingKOL")
                .build();
    }

    public String upsertEvent(UUID userId, KolAvailability a) throws IOException, GeneralSecurityException {
        var link = linkRepo.findByUserId(userId).orElseThrow();
        var client = buildClient(link.getRefreshToken());
        String calendarId = Optional.ofNullable(a.getGoogleCalendarId()).orElse("primary");

        Event event = new Event()
                .setSummary("Lịch làm việc KOL")
                .setDescription(a.getNote())
                .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(a.getStartAt().toInstant().toString())))
                .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(a.getEndAt().toInstant().toString())));

        Event result;
        if (a.getGoogleEventId() == null) {
            result = client.events().insert(calendarId, event).execute();
        } else {
            result = client.events().patch(calendarId, a.getGoogleEventId(), event).execute();
        }
        return result.getId();
    }

    public void deleteEvent(UUID userId, String googleEventId, String calendarId)
            throws IOException, GeneralSecurityException {
        var link = linkRepo.findByUserId(userId).orElseThrow();
        var client = buildClient(link.getRefreshToken());
        client.events().delete(Optional.ofNullable(calendarId).orElse("primary"), googleEventId).execute();
    }
}

