package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversation_members")
public class ConversationMember {
    @EmbeddedId
    private ConversationMemberId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 50)
    @Column(name = "role_snapshot", length = 50)
    private String roleSnapshot;

    @ColumnDefault("now()")
    @Column(name = "joined_at")
    private Instant joinedAt;

}

@Embeddable
@Data
class ConversationMemberId implements Serializable {
    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "user_id")
    private UUID userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationMemberId that)) return false;
        return conversationId.equals(that.conversationId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(conversationId, userId);
    }
}