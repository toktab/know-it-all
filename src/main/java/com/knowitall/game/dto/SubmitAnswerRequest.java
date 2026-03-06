package com.knowitall.game.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitAnswerRequest {
    private Long questionId;
    private String userAnswer;        // typed text, not A/B/C/D
    private Integer timeTakenSeconds;
}