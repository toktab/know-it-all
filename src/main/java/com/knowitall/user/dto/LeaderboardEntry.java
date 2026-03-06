package com.knowitall.user.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardEntry {
    private Integer rank;
    private String username;
    private Integer totalScore;
    private Integer gamesPlayed;
}