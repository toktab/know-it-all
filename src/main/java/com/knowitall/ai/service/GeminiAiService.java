package com.knowitall.ai.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GeminiAiService implements AiService {

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String model;

    private final RestTemplate restTemplate;
    private final JsonMapper objectMapper;

    @Override
    public GeneratedQuestion generateQuestion(String topic, int difficulty, List<String> previousQuestions) {
        String prompt = buildQuestionPrompt(topic, difficulty, previousQuestions);
        String raw = call(prompt);
        try {
            JsonNode node = objectMapper.readTree(clean(raw));
            return new GeneratedQuestion(
                    node.get("question").asString(),
                    node.get("correctAnswer").asString(),
                    prompt, raw
            );
        } catch (Exception e) {
            throw new RuntimeException("Gemini: failed to parse question: " + raw, e);
        }
    }

    @Override
    public List<String> getBreakdown(String question, String userAnswer, String correctAnswer) {
        String prompt = buildBreakdownPrompt(question, userAnswer, correctAnswer);
        String raw = call(prompt);
        try {
            JsonNode node = objectMapper.readTree(clean(raw));
            List<String> reasons = new ArrayList<>();
            for (JsonNode r : node.get("reasons")) reasons.add(r.asString());
            return reasons;
        } catch (Exception e) {
            return List.of("Correct answer: " + correctAnswer);
        }
    }

    private String call(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), Map.class);

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private String buildQuestionPrompt(String topic, int difficulty, List<String> previousQuestions) {
        String avoidSection = previousQuestions == null || previousQuestions.isEmpty() ? "" : """

                Already asked questions — do NOT repeat or rephrase any of these:
                %s
                """.formatted(previousQuestions.stream()
                .map(q -> "- " + q)
                .reduce("", (a, b) -> a + "\n" + b));

        return """
                Generate a challenging and interesting quiz question about "%s" with difficulty %d/10.

                Rules:
                - Do NOT ask "what is X" or "name this thing" type questions
                - Ask about specific facts, concepts, history, comparisons, or how things work
                - The question must have one clear, concise correct answer (1-5 words max)
                - Difficulty %d/10 means: %s
                - Never repeat obvious or generic questions
                - The player will type their answer in a text box — no multiple choice
                %s
                Respond ONLY with valid JSON, no markdown, no extra text:
                {
                  "question": "...",
                  "correctAnswer": "..."
                }
                """.formatted(topic, difficulty, difficulty, difficultyHint(difficulty), avoidSection);
    }

    private String difficultyHint(int difficulty) {
        if (difficulty <= 3) return "basic facts a beginner would know, but still interesting and specific";
        if (difficulty <= 6) return "intermediate knowledge requiring some study or experience";
        if (difficulty <= 8) return "advanced concepts that experts would know";
        return "expert-level, highly specific, obscure facts";
    }

    private String buildBreakdownPrompt(String question, String userAnswer, String correctAnswer) {
        return """
                A student answered a quiz question incorrectly.
                Question: %s
                Student answered: %s
                Correct answer: %s
                Give 2 to 4 short, clear reasons why the student was wrong.
                Respond ONLY with valid JSON, no markdown:
                { "reasons": ["reason 1", "reason 2"] }
                """.formatted(question, userAnswer, correctAnswer);
    }

    private String clean(String raw) {
        return raw.replaceAll("```json|```", "").trim();
    }
}