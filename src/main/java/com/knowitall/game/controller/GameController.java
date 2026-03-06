package com.knowitall.game.controller;

import com.knowitall.game.dto.*;
import com.knowitall.game.service.GameService;
import com.knowitall.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameSessionResponse> startGame(
            @AuthenticationPrincipal User user,
            @RequestBody StartGameRequest request) {
        return ResponseEntity.ok(gameService.startGame(user, request));
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<QuestionResponse> getNextQuestion(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(gameService.getNextQuestion(sessionId, user));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResultResponse> submitAnswer(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(gameService.submitAnswer(sessionId, user, request));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<GameSessionResponse> endSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(gameService.endSession(sessionId, user));
    }

    @GetMapping("/history")
    public ResponseEntity<List<GameSessionResponse>> getHistory(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gameService.getUserHistory(user));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<SessionDetailResponse> getSessionDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(gameService.getSessionDetail(sessionId, user));
    }
}