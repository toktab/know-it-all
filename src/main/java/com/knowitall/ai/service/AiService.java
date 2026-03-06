package com.knowitall.ai.service;

import java.util.List;

public interface AiService {
    GeneratedQuestion generateQuestion(String topic, int difficulty);
    List<String> getBreakdown(String question, String userAnswer,
                              String correctAnswer, String correctAnswerText);

    record GeneratedQuestion(
            String questionText,
            String optionA, String optionB, String optionC, String optionD,
            String correctAnswer, String correctAnswerText,
            String rawPrompt, String rawResponse
    ) {}
}