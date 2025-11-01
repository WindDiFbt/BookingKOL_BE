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
    public enum UserGender {
        Male,
        Female,
    }

    @Getter
    public enum Languages {
        vi,
        en,
    }

    @Getter
    public enum Roles {
        SUPER_ADMIN(1),
        ADMIN(2),
        USER(3),
        KOL(4),
        LIVE(5);
        private final Integer value;

        private Roles(Integer value) {
            this.value = value;
        }
    }

    // ---------------- KOL ----------------
    @Getter
    public enum CategoryKey {
        FASHION_BEAUTY,
        FOOD_BEVERAGE,
        TECH_DIGITAL
    }

    // ---------------- File ----------------
    @Getter
    public enum FileType {
        IMAGE,
        VIDEO,
        DOCUMENT,
        OTHER
    }

    @Getter
    public enum TargetType {
        AVATAR,
        PORTFOLIO,
        CONTRACT,
        COURSE_PACKAGE,
        SERVICE_PACKAGE,
        ATTACHMENTS,
        OTHER,
        CAMPAIGN
    }

    @Getter
    public enum FileStatus {
        ACTIVE,
        INACTIVE,
        DELETED
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
        PAID,
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
        PAID,
        COMPLETED,
        CANCELLED,
        EXPIRED,
        WAIT_FOR_REFUND,
        REFUNDED
    }

    @Getter
    public enum BookingRules {
        MIN_BOOKING_TIME(1),
        MAX_BOOKING_TIME(3),
        REST_TIME(1);
        private final Integer value;

        private BookingRules(Integer value) {
            this.value = value;
        }
    }

    @Getter
    public enum BookingType {
        SINGLE,
    }

    @Getter
    public enum KOLWorkTimeStatus {
        REQUESTED,
        ACCEPTED,
        IN_PROGRESS,
        CANCELLED,
        COMPLETED
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
    public enum PaymentStatus {
        PENDING,
        UNDERPAID,
        PAID,
        OVERPAID,
        EXPIRED,
        REFUNDED,
        CANCELLED
    }

    @Getter
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED,
        ORPHANED
    }

    @Getter
    public enum RefundStatus {
        PENDING,
        REFUNDED
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

    @Getter
    public enum PurchasedCourse {
        COURSEASSIGNED,    // khóa học đã được gửi
        NOTASSIGNED        // khóa học chưa được gửi
    }
}
