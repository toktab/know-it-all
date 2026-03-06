package com.knowitall.game.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private Integer difficulty;
    private Integer timeLimitSeconds;
}