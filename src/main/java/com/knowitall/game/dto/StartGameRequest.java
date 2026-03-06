package com.knowitall.game.dto;

import com.knowitall.ai.config.AiProvider;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StartGameRequest {
    private String topic;
    private Integer difficulty;
    private AiProvider aiProvider;
}