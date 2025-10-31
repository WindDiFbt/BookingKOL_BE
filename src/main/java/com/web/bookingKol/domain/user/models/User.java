package com.web.bookingKol.domain.user.models;

import com.web.bookingKol.domain.blog.Blog;
import com.web.bookingKol.domain.booking.models.Campaign;
import com.web.bookingKol.domain.booking.models.Dispute;
import com.web.bookingKol.domain.booking.models.Offer;
import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolPromotion;
import com.web.bookingKol.temp_models.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 50)
    @Column(name = "phone", length = 50)
    private String phone;

    @Size(max = 255)
    @Column(name = "password_hash")
    private String passwordHash;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 50)
    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Size(max = 64)
    @Column(name = "timezone", length = 64)
    private String timezone;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "actorUser")
    private Set<ActivityLog> activityLogs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Address> addresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<AdminMember> adminMembers = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private Brand brand;

    @OneToMany(mappedBy = "createdBy")
    private Set<Campaign> campaigns = new LinkedHashSet<>();

    @Column(name = "introduction", length = 1000)
    private String introduction;

    @OneToMany(mappedBy = "user")
    private Set<ConversationMember> conversationMembers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "raisedBy")
    private Set<Dispute> disputes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "uploader")
    private Set<File> files = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reviewerUser")
    private Set<KolFeedback> kolFeedbacks = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private KolProfile kolProfile;

    @OneToMany(mappedBy = "createdBy")
    private Set<KolPromotion> kolPromotions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Notification> notifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sentBy")
    private Set<Offer> offers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<UserQuestionLog> userQuestionLogs = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    private Set<Blog> blogs = new LinkedHashSet<>();
}