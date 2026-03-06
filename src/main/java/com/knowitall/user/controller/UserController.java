package com.knowitall.user.controller;

import com.knowitall.user.dto.LeaderboardEntry;
import com.knowitall.user.dto.UpdateUserRequest;
import com.knowitall.user.dto.UserResponse;
import com.knowitall.user.entity.User;
import com.knowitall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(user.getId(), request));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }
}