package com.knowitall.game.dto;

import com.knowitall.ai.config.AiProvider;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionDetailResponse {
    private Long sessionId;
    private String topic;
    private Integer difficulty;
    private AiProvider aiProvider;
    private Integer score;
    private Integer questionsAsked;
    private Integer correctAnswers;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<QuestionHistoryResponse> questions;
}