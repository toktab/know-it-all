package com.knowitall.ai.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CohereAiService implements AiService {

    @Value("${ai.cohere.api-key:}")
    private String apiKey;

    @Value("${ai.cohere.model:command-r-plus}")
    private String model;

    private static final String API_URL = "https://api.cohere.com/v2/chat";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public GeneratedQuestion generateQuestion(String topic, int difficulty) {
        String prompt = buildQuestionPrompt(topic, difficulty);
        String raw = call(prompt);
        try {
            JsonNode node = objectMapper.readTree(clean(raw));
            return new GeneratedQuestion(
                    node.get("question").asString(),
                    node.get("optionA").asString(),
                    node.get("optionB").asString(),
                    node.get("optionC").asString(),
                    node.get("optionD").asString(),
                    node.get("correctAnswer").asString(),
                    node.get("correctAnswerText").asString(),
                    prompt, raw
            );
        } catch (Exception e) {
            throw new RuntimeException("Cohere: failed to parse question: " + raw, e);
        }
    }

    @Override
    public List<String> getBreakdown(String question, String userAnswer,
                                     String correctAnswer, String correctAnswerText) {
        String prompt = buildBreakdownPrompt(question, userAnswer, correctAnswer, correctAnswerText);
        String raw = call(prompt);
        try {
            JsonNode node = objectMapper.readTree(clean(raw));
            List<String> reasons = new ArrayList<>();
            for (JsonNode r : node.get("reasons")) reasons.add(r.asString());
            return reasons;
        } catch (Exception e) {
            return List.of("Correct answer: " + correctAnswer + " — " + correctAnswerText);
        }
    }

    private String call(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Cohere v2 chat format
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    API_URL, new HttpEntity<>(body, headers), Map.class);

            // Response: { message: { content: [ { type: "text", text: "..." } ] } }
            Map<String, Object> message =
                    (Map<String, Object>) response.getBody().get("message");
            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) message.get("content");
            return (String) content.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Cohere API call failed: " + e.getMessage(), e);
        }
    }

    private String buildQuestionPrompt(String topic, int difficulty) {
        return """
                Generate a multiple-choice quiz question about "%s" with difficulty %d/10.
                Respond ONLY with valid JSON, no markdown, no extra text:
                {
                  "question": "...",
                  "optionA": "...",
                  "optionB": "...",
                  "optionC": "...",
                  "optionD": "...",
                  "correctAnswer": "A",
                  "correctAnswerText": "..."
                }
                """.formatted(topic, difficulty);
    }

    private String buildBreakdownPrompt(String question, String userAnswer,
                                        String correctAnswer, String correctAnswerText) {
        return """
                A student answered a quiz question incorrectly.
                Question: %s
                Student answered: %s
                Correct answer: %s — %s
                Give 2 to 4 short, clear reasons why the student was wrong.
                Respond ONLY with valid JSON, no markdown:
                { "reasons": ["reason 1", "reason 2"] }
                """.formatted(question, userAnswer, correctAnswer, correctAnswerText);
    }

    private String clean(String raw) {
        return raw.replaceAll("```json|```", "").trim();
    }
}