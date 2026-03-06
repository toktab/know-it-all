package com.knowitall.user.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Integer totalScore;
    private Integer gamesPlayed;
    private LocalDateTime createdAt;
}