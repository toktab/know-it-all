package com.knowitall.ai.service;

import java.util.List;

public interface AiService {
    GeneratedQuestion generateQuestion(String topic, int difficulty, List<String> previousQuestions);
    List<String> getBreakdown(String question, String userAnswer, String correctAnswer);

    record GeneratedQuestion(
            String questionText,
            String correctAnswer,
            String rawPrompt,
            String rawResponse
    ) {}
}