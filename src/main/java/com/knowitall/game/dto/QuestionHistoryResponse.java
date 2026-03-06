package com.knowitall.game.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionHistoryResponse {
    private Long questionId;
    private String questionText;
    private String correctAnswer;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer timeTakenSeconds;
    private List<String> breakdown;
    private LocalDateTime answeredAt;
}