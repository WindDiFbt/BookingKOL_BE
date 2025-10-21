package com.web.bookingKol.domain.kol.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.user.models.User;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KolAvailabilityDTO {

    private UUID id;
    private Instant startAt;
    private Instant endAt;
    private String status;

    private String note;

    private List<WorkTimeDTO> workTimes = new ArrayList<>();

    private UUID kolId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;

    public KolAvailabilityDTO() {
    }

    public KolAvailabilityDTO(KolAvailability availability) {
        this.id = availability.getId();
        this.startAt = availability.getStartAt();
        this.endAt = availability.getEndAt();
        this.status = availability.getStatus();

        if (availability.getWorkTimes() != null && !availability.getWorkTimes().isEmpty()) {
            this.workTimes = availability.getWorkTimes()
                    .stream()
                    .map(WorkTimeDTO::new)
                    .toList();
        }

        User user = availability.getKol().getUser();
        if (user != null) {
            this.kolId = availability.getKol().getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.avatarUrl = user.getAvatarUrl();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UUID getKolId() {
        return kolId;
    }

    public void setKolId(UUID kolId) {
        this.kolId = kolId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<WorkTimeDTO> getWorkTimes() {
        return workTimes;
    }

    public void setWorkTimes(List<WorkTimeDTO> workTimes) {
        this.workTimes = workTimes;
    }
}
