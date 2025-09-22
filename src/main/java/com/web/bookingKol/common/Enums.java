package com.web.bookingKol.common;

import lombok.Getter;

public class Enums {
    // ---------------- USER ----------------
    @Getter
    public enum UserStatus {
        ACTIVE,
        SUSPENDED,
        PENDING
    }

    @Getter
    public enum Roles {
        SUPER_ADMIN(1),
        ADMIN(2),
        USER(3),
        KOL(4);
        private final Integer value;

        private Roles(Integer value) {
            this.value = value;
        }
    }

    // ---------------- BOOKING ----------------
    @Getter
    public enum BookingStatus {
        DRAFT,
        REQUESTED,
        NEGOTIATING,
        ACCEPTED,
        REJECTED,
        CANCELLED,
        CONTRACT_SIGNED,
        IN_PROGRESS,
        DELIVERED,
        COMPLETED,
        DISPUTED,
        EXPIRED
    }

    @Getter
    public enum ContractStatus {
        DRAFT,
        SENT,
        SIGNED,
        CANCELLED,
        EXPIRED
    }

    // ---------------- PAYMENT ----------------
    @Getter
    public enum PaymentIntentStatus {
        REQUIRES_ACTION,
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        AUTHORIZED,
        CAPTURED,
        CANCELED,
        FAILED,
        EXPIRED
    }

    @Getter
    public enum PaymentMethodType {
        CARD,
        BANK_TRANSFER,
        EWALLET,
        COD,
        OTHER
    }

    @Getter
    public enum PayoutStatus {
        PENDING,
        PROCESSING,
        PAID,
        FAILED
    }

    // ---------------- PLATFORM ----------------
    @Getter
    public enum PlatformType {
        FACEBOOK,
        INSTAGRAM,
        TIKTOK,
        YOUTUBE,
        X,
        TWITCH,
        KUAISHOU,
        OTHER
    }

    // ---------------- DELIVERABLE ----------------
    @Getter
    public enum DeliverableType {
        POST,
        STORY,
        VIDEO,
        LIVESTREAM,
        SHORTS,
        REEL,
        TIKTOK_VIDEO,
        BLOG,
        OTHER
    }

    // ---------------- OFFER ----------------
    @Getter
    public enum OfferStatus {
        SENT,
        COUNTERED,
        ACCEPTED,
        REJECTED,
        WITHDRAWN,
        EXPIRED
    }

    @Getter
    public enum OfferType {
        LIVESTREAM,
        VIDEO_AD,
        BLOG_REVIEW,
        POST,
        STORY,
        OTHER
    }

    // ---------------- DISPUTE ----------------
    @Getter
    public enum DisputeStatus {
        OPEN,
        UNDER_REVIEW,
        RESOLVED,
        REJECTED
    }

    // ---------------- CAMPAIGN ----------------
    @Getter
    public enum CampaignStatus {
        PLANNED,
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }

    // ---------------- PROMO ----------------
    @Getter
    public enum PromoScope {
        GLOBAL,     // áp dụng cho mọi brand
        BRAND,      // chỉ 1 brand cụ thể
        CAMPAIGN    // chỉ 1 campaign cụ thể
    }
}
