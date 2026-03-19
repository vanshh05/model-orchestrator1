package com.yourcompany.orchestrator.analyzer;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.yourcompany.orchestrator.core.TaskType;

import jakarta.annotation.PostConstruct;

@Component
public class PromptAnalyzer {

    @Value("${huggingface.api.token}")
    private String apiToken;

    private RestClient restClient;

    // Confidence threshold — if regex score >= this, skip AI call
    private static final int HIGH_CONFIDENCE = 2;

    // ── Regex patterns per task type ──────────────────────────────────────────

    private static final List<Pattern> CODE_PATTERNS = List.of(
        Pattern.compile("\\b(write|create|implement|build|code|program|fix|debug|refactor|optimize)\\b.*\\b(function|method|class|algorithm|script|code|program|api|endpoint|query|sql)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(java|python|javascript|typescript|c\\+\\+|kotlin|golang|rust|php|ruby|swift)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(for loop|while loop|recursion|sorting|binary search|linked list|stack|queue|hashmap|array|string manipulation)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(null pointer|exception|bug|compile error|runtime error|stacktrace)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(spring boot|react|node\\.js|django|flask|hibernate|maven|gradle)\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> MATH_PATTERNS = List.of(
        Pattern.compile("\\b(solve|calculate|compute|evaluate|simplify|prove|derive)\\b.*\\b(equation|integral|derivative|matrix|vector|probability|theorem|formula)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(arithmetic|algebra|calculus|geometry|trigonometry|statistics|linear algebra|differential equation)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(\\d+\\s*[+\\-*/^]\\s*\\d+|square root|factorial|logarithm|percentage of)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(sin|cos|tan|log|ln|sqrt|pi|infinity)\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> IMAGE_PATTERNS = List.of(
        Pattern.compile("\\b(generate|create|draw|design|make|produce)\\b.*\\b(image|picture|photo|illustration|logo|icon|artwork|graphic|poster|banner)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(visualize|render|paint|sketch|portrait|landscape|thumbnail)\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> VIDEO_PATTERNS = List.of(
        Pattern.compile("\\b(create|generate|make|produce)\\b.*\\b(video|animation|clip|reel|short film|motion)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(animate|storyboard|screenplay|scene|frame rate)\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> TRANSLATION_PATTERNS = List.of(
        Pattern.compile("\\b(translate|convert|localize)\\b.*\\b(to|into|from)\\b.*\\b(english|french|spanish|german|chinese|japanese|hindi|arabic|portuguese|italian|korean|russian)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(translation of|in (french|spanish|german|chinese|japanese|hindi|arabic|portuguese|italian|korean|russian))\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> SUMMARIZATION_PATTERNS = List.of(
        Pattern.compile("\\b(summarize|summarise|give me a summary|brief summary|short summary|tldr|tl;dr)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(key points|main points|overview|highlight|condense|shorten|in brief|in short)\\b.*\\b(this|the|following|article|text|paragraph|document|passage)\\b", Pattern.CASE_INSENSITIVE)
    );

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl("https://router.huggingface.co/v1")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("x-wait-for-model", "true")
                .build();
    }

    public TaskType analyze(String prompt) {
        // Stage 1 — regex matching
        TaskType regexResult = matchWithRegex(prompt);
        if (regexResult != null) {
            System.out.println("[PromptAnalyzer] Regex matched: " + regexResult);
            return regexResult;
        }

        // Stage 2 — AI classification fallback
        System.out.println("[PromptAnalyzer] Regex uncertain, calling AI classifier...");
        try {
            return classifyWithAI(prompt);
        } catch (Exception e) {
            System.err.println("[PromptAnalyzer] AI classification failed: " + e.getMessage());
            return TaskType.GENERAL_CHAT;
        }
    }

    private TaskType matchWithRegex(String prompt) {
        if (countMatches(prompt, CODE_PATTERNS) >= HIGH_CONFIDENCE) return TaskType.CODE;
        if (countMatches(prompt, MATH_PATTERNS) >= HIGH_CONFIDENCE) return TaskType.MATH;
        if (countMatches(prompt, IMAGE_PATTERNS) >= 1) return TaskType.IMAGE;
        if (countMatches(prompt, VIDEO_PATTERNS) >= 1) return TaskType.VIDEO;
        if (countMatches(prompt, TRANSLATION_PATTERNS) >= 1) return TaskType.TRANSLATION;
        if (countMatches(prompt, SUMMARIZATION_PATTERNS) >= 1) return TaskType.SUMMARIZATION;
        if (countMatches(prompt, CODE_PATTERNS) >= 1) return TaskType.CODE;
        if (countMatches(prompt, MATH_PATTERNS) >= 1) return TaskType.MATH;

        // Not confident enough — let AI decide
        return null;
    }

    private int countMatches(String prompt, List<Pattern> patterns) {
        int count = 0;
        for (Pattern p : patterns) {
            if (p.matcher(prompt).find()) count++;
        }
        return count;
    }

    private TaskType classifyWithAI(String prompt) {
        String systemPrompt = """
                You are a task classifier. Classify the user's prompt into exactly ONE of these categories:
                CODE, MATH, IMAGE, VIDEO, TRANSLATION, SUMMARIZATION, GENERAL_CHAT
                
                Rules:
                - CODE: writing, debugging, or explaining code/algorithms
                - MATH: solving equations, calculations, mathematical proofs
                - IMAGE: generating or describing images/visuals
                - VIDEO: generating or creating video/animation content
                - TRANSLATION: translating text between languages
                - SUMMARIZATION: summarizing or condensing text/articles
                - GENERAL_CHAT: everything else
                
                Respond with ONLY the category name. No explanation. No punctuation.
                """;

        Map<String, Object> requestBody = Map.of(
            "model", "meta-llama/Llama-3.1-8B-Instruct:cerebras",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", 10,
            "temperature", 0.1
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response != null) {
            List<Map> choices = (List<Map>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map message = (Map) choices.get(0).get("message");
                if (message != null) {
                    String result = message.get("content").toString().trim().toUpperCase();
                    System.out.println("[PromptAnalyzer] AI classified as: " + result);
                    return parseTaskType(result);
                }
            }
        }
        return TaskType.GENERAL_CHAT;
    }

    private TaskType parseTaskType(String value) {
        try {
            return TaskType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // AI might return something like "CODE." or "CODE task"
            for (TaskType type : TaskType.values()) {
                if (value.contains(type.name())) return type;
            }
            return TaskType.GENERAL_CHAT;
        }
    }
}