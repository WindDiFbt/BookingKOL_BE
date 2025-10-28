package com.web.bookingKol.domain.course.models;

import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchased_course_package")
public class PurchasedCoursePackage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_package_id", nullable = false)
    private CoursePackage coursePackage;

    @Column(name = "current_price")
    private Long currentPrice;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "is_paid")
    private Boolean isPaid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "purchased_course_number")
    private String purchasedCourseNumber;
}