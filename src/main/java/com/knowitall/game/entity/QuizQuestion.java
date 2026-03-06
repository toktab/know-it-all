package com.knowitall.game.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Builder.Default
    @Column(name = "asked_at")
    private LocalDateTime askedAt = LocalDateTime.now();

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "answer_breakdown", columnDefinition = "TEXT")
    private String answerBreakdown;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "ai_prompt",   columnDefinition = "TEXT")
    private String aiPrompt;

    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;
}