package com.knowitall.ai.service;

import com.knowitall.ai.config.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiServiceRouter {

    private final GeminiAiService geminiAiService;
    private final GroqAiService   groqAiService;
    private final CohereAiService cohereAiService;

    public AiService.GeneratedQuestion generateQuestion(AiProvider provider,
                                                        String topic, int difficulty,
                                                        List<String> previousQuestions) {
        return resolve(provider).generateQuestion(topic, difficulty, previousQuestions);
    }

    public List<String> getBreakdown(AiProvider provider,
                                     String question, String userAnswer, String correctAnswer) {
        return resolve(provider).getBreakdown(question, userAnswer, correctAnswer);
    }

    private AiService resolve(AiProvider provider) {
        return switch (provider) {
            case GEMINI -> geminiAiService;
            case GROQ   -> groqAiService;
            case COHERE -> cohereAiService;
        };
    }
}