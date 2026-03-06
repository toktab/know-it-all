package com.knowitall.user.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRequest {
    private String email;
    private String currentPassword;
    private String newPassword;
}