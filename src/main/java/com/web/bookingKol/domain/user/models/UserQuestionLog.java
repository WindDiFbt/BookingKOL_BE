package com.web.bookingKol.domain.user.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_question_logs")
public class UserQuestionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "question", length = Integer.MAX_VALUE)
    private String question;

    @Column(name = "answer", length = Integer.MAX_VALUE)
    private String answer;

    @Size(max = 50)
    @Column(name = "answer_type", length = 50)
    private String answerType;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @ColumnDefault("false")
    @Column(name = "is_flagged")
    private Boolean isFlagged;

}