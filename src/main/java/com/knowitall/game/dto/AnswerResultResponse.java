package com.knowitall.game.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnswerResultResponse {
    private Boolean correct;
    private String correctAnswer;
    private List<String> breakdown;
    private Integer pointsEarned;
    private Integer currentScore;
    private String encouragement;
}