package com.knowitall.game.entity;

import com.knowitall.ai.config.AiProvider;
import com.knowitall.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_provider", nullable = false)
    private AiProvider aiProvider;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    @Column(name = "questions_asked")
    private Integer questionsAsked = 0;

    @Builder.Default
    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @Builder.Default
    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED
    }
}