package com.knowitall.ai.service;

import org.springframework.context.annotation.Primary;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Primary
@Service
@RequiredArgsConstructor
public class GroqAiService implements AiService {

    @Value("${ai.groq.api-key:}")
    private String apiKey;

    @Value("${ai.groq.model:llama-3.3-70b-versatile}")
    private String model;

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

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
            throw new RuntimeException("Groq: failed to parse question: " + raw, e);
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 400
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    API_URL, new HttpEntity<>(body, headers), Map.class);

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, String> message =
                    (Map<String, String>) choices.get(0).get("message");
            return message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }

    private String buildQuestionPrompt(String topic, int difficulty, List<String> previousQuestions) {
        String avoidSection = (previousQuestions == null || previousQuestions.isEmpty()) ? "" : """

                ══ QUESTIONS ALREADY ASKED — DO NOT REPEAT OR REPHRASE ANY OF THESE ══
                %s
                ══════════════════════════════════════════════════════════════════════
                """.formatted(previousQuestions.stream()
                .map(q -> "  • " + q)
                .reduce("", (a, b) -> a + "\n" + b));

        return """
                You are a quiz question generator for a trivia game. Your job is to produce ONE high-quality quiz question.

                ════════════════════════════════════════
                TOPIC: "%s"
                DIFFICULTY: %d out of 10
                ════════════════════════════════════════

                ── WHAT THE DIFFICULTY LEVELS MEAN ──────────────────────────────────
                1–3  (Easy)    : A curious beginner could answer this after light reading.
                               Example topic "Astronomy" → "What is the closest star to Earth?" → "Sun"
                4–6  (Medium)  : Requires genuine study or real interest in the topic.
                               Example topic "Astronomy" → "What year did Voyager 1 leave the solar system?" → "2012"
                7–9  (Hard)    : Expert-level. Only enthusiasts or specialists would know this.
                               Example topic "Astronomy" → "What is the Chandrasekhar limit?" → "1.4 solar masses"
                10   (Expert)  : Obscure, specific, and punishing. Almost nobody gets this right.
                               Example topic "Astronomy" → "What is the name of the radio signal detected by Big Ear in 1977?" → "Wow signal"

                ── STRICT RULES FOR THE QUESTION ────────────────────────────────────
                ✅ DO:
                  - Ask about a specific fact, mechanism, historical event, comparison, origin, record, or consequence
                  - Make the question genuinely interesting and surprising — something a player will find rewarding to learn
                  - Ensure there is ONE and only ONE correct answer
                  - The answer must be SHORT: 1 to 5 words maximum (a name, number, place, term, or date)
                  - Scale specificity and obscurity to the difficulty level

                ❌ DO NOT:
                  - Ask "What is X?" or "What does X stand for?" or "Name the X" — these are boring definition questions
                  - Ask "Who invented/discovered X?" for famous inventors (too generic)
                  - Ask questions where the answer could be multiple things (ambiguous)
                  - Ask questions where the answer is a long sentence or explanation
                  - Ask questions that require opinion or estimation
                  - Use multiple choice — the player types a free-text answer
                %s
                ── OUTPUT FORMAT ────────────────────────────────────────────────────
                Respond with ONLY a JSON object. No markdown fences, no extra text, no commentary.
                The JSON must have exactly these two keys:

                {
                  "question": "Your question here?",
                  "correctAnswer": "Short answer here"
                }

                The "correctAnswer" must be the minimal correct answer a player would type.
                Do NOT include articles like "The" unless they are essential (e.g., "The Beatles" is fine, "The Sun" should just be "Sun").
                """.formatted(topic, difficulty, avoidSection);
    }

    private String buildBreakdownPrompt(String question, String userAnswer, String correctAnswer) {
        return """
                A player just answered a quiz question incorrectly. Your job is to explain why they were wrong
                in a way that is educational, specific, and helpful — not generic or condescending.

                ════════════════════════════════════════
                QUESTION    : %s
                PLAYER SAID : %s
                CORRECT ANS : %s
                ════════════════════════════════════════

                Write 2 to 4 short reasons that explain the mistake. Each reason should:
                  - Be specific to THIS question and answer — not generic advice like "read more carefully"
                  - Teach the player something real about why the correct answer is what it is
                  - Be written in plain English, 1–2 sentences max per reason
                  - NOT be condescending or lecture-y — keep it factual and informative

                ── OUTPUT FORMAT ────────────────────────────────────────────────────
                Respond with ONLY a JSON object. No markdown, no extra text:

                { "reasons": ["reason 1", "reason 2", "reason 3"] }
                """.formatted(question, userAnswer, correctAnswer);
    }

    private String clean(String raw) {
        return raw.replaceAll("```json|```", "").trim();
    }
}