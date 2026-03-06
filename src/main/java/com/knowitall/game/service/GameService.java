package com.knowitall.game.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.knowitall.ai.service.AiService;
import com.knowitall.ai.service.AiServiceRouter;
import com.knowitall.game.dto.*;
import com.knowitall.game.entity.GameSession;
import com.knowitall.game.entity.QuizQuestion;
import com.knowitall.game.repository.GameSessionRepository;
import com.knowitall.game.repository.QuizQuestionRepository;
import com.knowitall.user.entity.User;
import com.knowitall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository sessionRepository;
    private final QuizQuestionRepository questionRepository;
    private final AiServiceRouter aiRouter;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Transactional
    public GameSessionResponse startGame(User user, StartGameRequest request) {
        if (request.getDifficulty() < 0 || request.getDifficulty() > 10)
            throw new RuntimeException("Difficulty must be between 0 and 10");
        if (request.getAiProvider() == null)
            throw new RuntimeException("Please select an AI provider");

        GameSession session = GameSession.builder()
                .user(user)
                .topic(request.getTopic())
                .difficulty(request.getDifficulty())
                .aiProvider(request.getAiProvider())
                .build();

        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public QuestionResponse getNextQuestion(Long sessionId, User user) {
        GameSession session = getSession(sessionId, user);

        AiService.GeneratedQuestion generated =
                aiRouter.generateQuestion(session.getAiProvider(), session.getTopic(), session.getDifficulty());

        QuizQuestion question = QuizQuestion.builder()
                .session(session)
                .questionText(generated.questionText())
                .correctAnswer(generated.correctAnswer())
                .aiPrompt(generated.rawPrompt())
                .aiResponse(generated.rawResponse())
                .build();

        question = questionRepository.save(question);

        return QuestionResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .difficulty(session.getDifficulty())
                .timeLimitSeconds(getTimeLimit(session.getDifficulty()))
                .build();
    }

    @Transactional
    public AnswerResultResponse submitAnswer(Long sessionId, User user, SubmitAnswerRequest request) {
        GameSession session = getSession(sessionId, user);

        QuizQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        boolean correct = isCorrect(request.getUserAnswer(), question.getCorrectAnswer());

        int points = 0;
        List<String> breakdown = List.of();

        if (correct) {
            int timeLimit = getTimeLimit(session.getDifficulty());
            points = calculatePoints(session.getDifficulty(), request.getTimeTakenSeconds(), timeLimit);
            session.setScore(session.getScore() + points);
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        } else {
            breakdown = aiRouter.getBreakdown(
                    session.getAiProvider(),
                    question.getQuestionText(),
                    request.getUserAnswer(),
                    question.getCorrectAnswer()
            );
        }

        session.setQuestionsAsked(session.getQuestionsAsked() + 1);
        sessionRepository.save(session);

        question.setUserAnswer(request.getUserAnswer());
        question.setIsCorrect(correct);
        question.setTimeTakenSeconds(request.getTimeTakenSeconds());
        question.setAnsweredAt(LocalDateTime.now());
        if (!breakdown.isEmpty()) {
            try { question.setAnswerBreakdown(objectMapper.writeValueAsString(breakdown)); }
            catch (Exception ignored) {}
        }
        questionRepository.save(question);

        return AnswerResultResponse.builder()
                .correct(correct)
                .correctAnswer(question.getCorrectAnswer())
                .breakdown(breakdown)
                .pointsEarned(points)
                .currentScore(session.getScore())
                .encouragement(correct ? getEncouragement(points) : "Not quite! See the explanation below.")
                .build();
    }

    @Transactional
    public GameSessionResponse endSession(Long sessionId, User user) {
        GameSession session = getSession(sessionId, user);
        session.setStatus(GameSession.SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);
        userService.addScore(user.getId(), session.getScore());
        return toSessionResponse(session);
    }

    public List<GameSessionResponse> getUserHistory(User user) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(user.getId())
                .stream().map(this::toSessionResponse).toList();
    }

    public SessionDetailResponse getSessionDetail(Long sessionId, User user) {
        GameSession session = getSession(sessionId, user);
        List<QuizQuestion> questions = questionRepository.findBySessionIdOrderByAskedAt(sessionId);

        List<QuestionHistoryResponse> qList = questions.stream()
                .filter(q -> q.getAnsweredAt() != null)
                .map(q -> {
                    List<String> bd = List.of();
                    if (q.getAnswerBreakdown() != null) {
                        try { bd = objectMapper.readValue(q.getAnswerBreakdown(), new TypeReference<>() {}); }
                        catch (Exception ignored) {}
                    }
                    return QuestionHistoryResponse.builder()
                            .questionId(q.getId())
                            .questionText(q.getQuestionText())
                            .correctAnswer(q.getCorrectAnswer())
                            .userAnswer(q.getUserAnswer())
                            .isCorrect(q.getIsCorrect())
                            .timeTakenSeconds(q.getTimeTakenSeconds())
                            .breakdown(bd)
                            .answeredAt(q.getAnsweredAt())
                            .build();
                }).toList();

        return SessionDetailResponse.builder()
                .sessionId(session.getId())
                .topic(session.getTopic())
                .difficulty(session.getDifficulty())
                .aiProvider(session.getAiProvider())
                .score(session.getScore())
                .questionsAsked(session.getQuestionsAsked())
                .correctAnswers(session.getCorrectAnswers())
                .status(session.getStatus().name())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .questions(qList)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Fuzzy match — trims whitespace, ignores case, ignores punctuation
    private boolean isCorrect(String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) return false;
        String a = normalize(userAnswer);
        String b = normalize(correctAnswer);
        return a.equals(b) || b.contains(a) || a.contains(b);
    }

    private String normalize(String s) {
        return s.toLowerCase().trim().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
    }

    private GameSession getSession(Long sessionId, User user) {
        return sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    private int getTimeLimit(int difficulty) {
        if (difficulty <= 3) return 45;
        if (difficulty <= 6) return 30;
        return 20;
    }

    private int calculatePoints(int difficulty, int timeTaken, int timeLimit) {
        int base = 10 + (difficulty * 5);
        double timeBonus = Math.max(0.0, (double)(timeLimit - timeTaken) / timeLimit);
        return (int)(base * (1 + timeBonus * 0.5));
    }

    private String getEncouragement(int points) {
        if (points >= 70) return "🔥 Blazing fast! Bonus points earned!";
        if (points >= 50) return "⚡ Great answer!";
        return "✅ Correct!";
    }

    private GameSessionResponse toSessionResponse(GameSession s) {
        return GameSessionResponse.builder()
                .sessionId(s.getId())
                .topic(s.getTopic())
                .difficulty(s.getDifficulty())
                .aiProvider(s.getAiProvider())
                .score(s.getScore())
                .questionsAsked(s.getQuestionsAsked())
                .correctAnswers(s.getCorrectAnswers())
                .status(s.getStatus().name())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .build();
    }
}