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

    // ── Confidence thresholds ─────────────────────────────────────────────────
    // STRONG: one pattern match is enough to be confident
    // HIGH: two pattern matches needed for confidence
    private static final int STRONG_CONFIDENCE = 1;
    private static final int HIGH_CONFIDENCE = 2;

    // ── CODE patterns ─────────────────────────────────────────────────────────
    private static final List<Pattern> CODE_PATTERNS = List.of(
        // Action + artifact (write a method, build an API, fix the bug)
        Pattern.compile("\\b(write|create|implement|build|code|program|fix|debug|refactor|optimize|develop|design)\\b.{0,40}\\b(function|method|class|algorithm|script|program|api|endpoint|query|sql|database|schema)\\b", Pattern.CASE_INSENSITIVE),
        // Programming languages — very strong signal
        Pattern.compile("\\b(java|python|javascript|typescript|c\\+\\+|c#|kotlin|golang|go|rust|php|ruby|swift|scala|r |bash|shell|html|css)\\b", Pattern.CASE_INSENSITIVE),
        // Data structures and algorithms
        Pattern.compile("\\b(for loop|while loop|recursion|recursive|sorting|binary search|linked list|stack|queue|hashmap|hash map|hash table|array|string manipulation|tree|graph|dynamic programming|big o)\\b", Pattern.CASE_INSENSITIVE),
        // Error types
        Pattern.compile("\\b(null pointer|nullpointerexception|exception|bug|compile error|runtime error|stacktrace|stack trace|syntax error|type error|index out of bounds)\\b", Pattern.CASE_INSENSITIVE),
        // Frameworks and tools
        Pattern.compile("\\b(spring boot|spring|react|angular|vue|node\\.js|nodejs|django|flask|express|hibernate|maven|gradle|docker|kubernetes|git|rest api|graphql|microservice)\\b", Pattern.CASE_INSENSITIVE),
        // Code concepts
        Pattern.compile("\\b(object oriented|oop|inheritance|polymorphism|encapsulation|interface|abstract|design pattern|singleton|factory|observer|mvc|crud|solid principles)\\b", Pattern.CASE_INSENSITIVE),
        // Explain code concepts
        Pattern.compile("\\b(explain|how does|what is|difference between).{0,30}\\b(recursion|polymorphism|inheritance|abstraction|interface|class|object|pointer|memory|thread|async|callback|promise|closure|lambda)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── MATH patterns ─────────────────────────────────────────────────────────
    private static final List<Pattern> MATH_PATTERNS = List.of(
        // Solve/calculate + math concept
        Pattern.compile("\\b(solve|calculate|compute|evaluate|simplify|prove|derive|find|what is).{0,30}\\b(equation|integral|derivative|matrix|vector|probability|theorem|formula|expression|series|limit|function|graph)\\b", Pattern.CASE_INSENSITIVE),
        // Math branches
        Pattern.compile("\\b(arithmetic|algebra|calculus|geometry|trigonometry|statistics|linear algebra|differential equation|number theory|set theory|combinatorics|permutation|combination)\\b", Pattern.CASE_INSENSITIVE),
        // Math operations and symbols
        Pattern.compile("\\b(square root|factorial|logarithm|percentage of|ratio of|proportion|prime number|prime factors|greatest common|least common|mean|median|mode|standard deviation|variance)\\b", Pattern.CASE_INSENSITIVE),
        // Trig functions and constants
        Pattern.compile("\\b(sin|cos|tan|cot|sec|csc|log|ln|sqrt|pi|euler|infinity|summation|sigma|theta|alpha|beta|gamma)\\b", Pattern.CASE_INSENSITIVE),
        // Simple arithmetic expressions like "what is 25 * 4" or "15 + 37"
        Pattern.compile("what is\\s+\\d+\\s*[+\\-*/^%]\\s*\\d+", Pattern.CASE_INSENSITIVE),
        // Percentage calculations
        Pattern.compile("\\b(\\d+\\s*%\\s*(of|off|increase|decrease)|calculate\\s+\\d+|\\d+\\s*percent)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── IMAGE patterns ────────────────────────────────────────────────────────
    private static final List<Pattern> IMAGE_PATTERNS = List.of(
        // Generate/create + visual artifact
        Pattern.compile("\\b(generate|create|draw|design|make|produce|show me|give me)\\b.{0,40}\\b(image|picture|photo|illustration|logo|icon|artwork|graphic|poster|banner|diagram|chart|infographic|wallpaper|avatar|thumbnail)\\b", Pattern.CASE_INSENSITIVE),
        // Visual creation verbs
        Pattern.compile("\\b(visualize|render|paint|sketch|illustrate|photoshop|generate an image|create an image|draw me|design me)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── VIDEO patterns ────────────────────────────────────────────────────────
    private static final List<Pattern> VIDEO_PATTERNS = List.of(
        // Create + video artifact
        Pattern.compile("\\b(create|generate|make|produce|record|film|shoot)\\b.{0,40}\\b(video|animation|clip|reel|short film|motion graphic|gif|timelapse|screencast)\\b", Pattern.CASE_INSENSITIVE),
        // Video concepts
        Pattern.compile("\\b(animate|storyboard|screenplay|scene|frame rate|video script|video edit|subtitle|caption)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── TRANSLATION patterns ──────────────────────────────────────────────────
    private static final List<Pattern> TRANSLATION_PATTERNS = List.of(
        // Translate/convert + language
        Pattern.compile("\\b(translate|convert|localize|say|write)\\b.{0,50}\\b(english|french|spanish|german|chinese|japanese|hindi|arabic|portuguese|italian|korean|russian|turkish|dutch|swedish|polish|ukrainian)\\b", Pattern.CASE_INSENSITIVE),
        // "in [language]" phrasing
        Pattern.compile("\\bin\\s+(french|spanish|german|chinese|japanese|hindi|arabic|portuguese|italian|korean|russian|turkish|dutch|swedish|polish|ukrainian)\\b", Pattern.CASE_INSENSITIVE),
        // "how do you say X in Y"
        Pattern.compile("\\bhow (do you|to) say\\b.{0,50}\\bin\\b", Pattern.CASE_INSENSITIVE),
        // "what is X in Y language"
        Pattern.compile("\\bwhat is.{0,30}in (french|spanish|german|chinese|japanese|hindi|arabic|portuguese|italian|korean|russian)\\b", Pattern.CASE_INSENSITIVE),
        // Direct translation request
        Pattern.compile("\\b(translate this|translation of|translated to|translated into)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── SUMMARIZATION patterns ────────────────────────────────────────────────
    private static final List<Pattern> SUMMARIZATION_PATTERNS = List.of(
        // Direct summarize request
        Pattern.compile("\\b(summarize|summarise|sum up|recap|recapitulate)\\b", Pattern.CASE_INSENSITIVE),
        // TL;DR variants
        Pattern.compile("\\b(tldr|tl;dr|tl\\s*dr|too long|give me the gist|give me a summary|brief summary|short summary|quick summary)\\b", Pattern.CASE_INSENSITIVE),
        // Key points extraction
        Pattern.compile("\\b(key points|main points|main ideas|key takeaways|important points|highlights|bullet points)\\b.{0,30}\\b(of|from|about|in|this|the|following)\\b", Pattern.CASE_INSENSITIVE),
        // Condense/shorten
        Pattern.compile("\\b(condense|shorten|compress|simplify|distill|extract the|overview of|synopsis of|abstract of)\\b", Pattern.CASE_INSENSITIVE),
        // "summarize this/the following"
        Pattern.compile("\\b(summarize|summarise|summary).{0,20}\\b(this|the|following|below|above|article|text|paragraph|document|passage|content|essay|report|paper)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── GENERAL_CHAT patterns ─────────────────────────────────────────────────
    // Catches common conversational prompts so they skip the AI fallback
    private static final List<Pattern> GENERAL_CHAT_PATTERNS = List.of(
        // What is / explain / tell me about
        Pattern.compile("\\b(what is|what are|what was|what were|who is|who are|who was|where is|where are|when did|when was|why is|why are|why does|how does|how do|how did|how can)\\b", Pattern.CASE_INSENSITIVE),
        // Explain / describe / define
        Pattern.compile("\\b(explain|describe|define|tell me about|tell me more|give me information|give me details|can you tell|help me understand)\\b", Pattern.CASE_INSENSITIVE),
        // Opinion / recommendation
        Pattern.compile("\\b(what do you think|what's your opinion|recommend|suggest|advice|should i|is it worth|pros and cons|advantages|disadvantages|compare|difference between|versus|vs\\b)\\b", Pattern.CASE_INSENSITIVE),
        // History / facts
        Pattern.compile("\\b(history of|origin of|invented by|discovered by|founded by|capital of|population of|known for|famous for)\\b", Pattern.CASE_INSENSITIVE)
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

        // Stage 2 — AI classification fallback (only for truly ambiguous prompts)
        System.out.println("[PromptAnalyzer] Regex uncertain, calling AI classifier...");
        try {
            return classifyWithAI(prompt);
        } catch (Exception e) {
            System.err.println("[PromptAnalyzer] AI classification failed: " + e.getMessage());
            return TaskType.GENERAL_CHAT;
        }
    }

    private TaskType matchWithRegex(String prompt) {

        // IMAGE and VIDEO — one match is enough (very specific patterns)
        if (countMatches(prompt, IMAGE_PATTERNS) >= STRONG_CONFIDENCE) return TaskType.IMAGE;
        if (countMatches(prompt, VIDEO_PATTERNS) >= STRONG_CONFIDENCE) return TaskType.VIDEO;

        // TRANSLATION — one match is enough (language names are very specific)
        if (countMatches(prompt, TRANSLATION_PATTERNS) >= STRONG_CONFIDENCE) return TaskType.TRANSLATION;

        // SUMMARIZATION — one match is enough
        if (countMatches(prompt, SUMMARIZATION_PATTERNS) >= STRONG_CONFIDENCE) return TaskType.SUMMARIZATION;

        // CODE — one strong match is enough (language names, frameworks are very specific)
        int codeScore = countMatches(prompt, CODE_PATTERNS);
        if (codeScore >= STRONG_CONFIDENCE) return TaskType.CODE;

        // MATH — one strong match is enough (math terms are very specific)
        int mathScore = countMatches(prompt, MATH_PATTERNS);
        if (mathScore >= STRONG_CONFIDENCE) return TaskType.MATH;

        // GENERAL_CHAT — catch common conversational patterns
        // to avoid unnecessary AI calls
        if (countMatches(prompt, GENERAL_CHAT_PATTERNS) >= STRONG_CONFIDENCE) return TaskType.GENERAL_CHAT;

        // Truly ambiguous — let AI decide
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
                - CODE: writing, debugging, or explaining code/algorithms/programming concepts
                - MATH: solving equations, calculations, mathematical proofs, arithmetic
                - IMAGE: generating or describing images/visuals/graphics
                - VIDEO: generating or creating video/animation content
                - TRANSLATION: translating text between languages
                - SUMMARIZATION: summarizing or condensing text/articles/documents
                - GENERAL_CHAT: everything else including questions, explanations, opinions

                Respond with ONLY the category name. No explanation. No punctuation. No extra words.
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
            for (TaskType type : TaskType.values()) {
                if (value.contains(type.name())) return type;
            }
            return TaskType.GENERAL_CHAT;
        }
    }
}