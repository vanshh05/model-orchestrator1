package com.yourcompany.orchestrator.service;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.yourcompany.orchestrator.core.ModelRule;
import com.yourcompany.orchestrator.repository.ModelRuleRepository;

@Service
public class BenchmarkUpdaterService {

    private final ModelRuleRepository ruleRepository;
    private final RestClient restClient;

    public BenchmarkUpdaterService(ModelRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        this.restClient = RestClient.builder()
                .baseUrl("https://huggingface.co/api")
                .build();
    }

    // Runs every 24 hours at midnight
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void updateBenchmarkScores() {
        System.out.println("[BenchmarkUpdater] Starting daily benchmark score update...");

        // Get all unique model names from DB
        List<ModelRule> allRules = ruleRepository.findAll();

        allRules.stream()
                .filter(rule -> rule.getProviderName().equals("huggingface"))
                .map(ModelRule::getModelName)
                .distinct()
                .forEach(modelName -> {
                    try {
                        // Strip the :provider suffix for the API call
                        // e.g. "meta-llama/Llama-3.1-8B-Instruct:cerebras" → "meta-llama/Llama-3.1-8B-Instruct"
                        String cleanModelName = modelName.contains(":")
                                ? modelName.substring(0, modelName.lastIndexOf(":"))
                                : modelName;

                        double score = fetchBenchmarkScore(cleanModelName);
                        if (score > 0) {
                            updateExternalScore(modelName, score);
                            System.out.println("[BenchmarkUpdater] Updated " + modelName + " → score: " + score);
                        }
                    } catch (Exception e) {
                        System.err.println("[BenchmarkUpdater] Failed for " + modelName + ": " + e.getMessage());
                    }
                });

        System.out.println("[BenchmarkUpdater] Done.");
    }

    private double fetchBenchmarkScore(String modelName) {
        try {
            // HuggingFace model API returns model metadata including eval results
            Map response = restClient.get()
                    .uri("/models/" + modelName)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return 0.0;

            // Extract eval results if available
            List<Map> evalResults = (List<Map>) response.get("cardData");
            if (evalResults == null) {
                // Fallback: use model downloads/likes as a popularity proxy score (normalized)
                Object downloads = response.get("downloads");
                Object likes = response.get("likes");

                double downloadScore = downloads != null
                        ? Math.min(((Number) downloads).doubleValue() / 1_000_000.0 * 50, 50)
                        : 0;
                double likeScore = likes != null
                        ? Math.min(((Number) likes).doubleValue() / 10_000.0 * 50, 50)
                        : 0;

                return downloadScore + likeScore;
            }

            return 0.0;

        } catch (Exception e) {
            System.err.println("[BenchmarkUpdater] API error for " + modelName + ": " + e.getMessage());
            return 0.0;
        }
    }

    private void updateExternalScore(String modelName, double externalScore) {
        List<ModelRule> rules = ruleRepository.findAll().stream()
                .filter(r -> r.getModelName().equals(modelName))
                .toList();

        for (ModelRule rule : rules) {
            double liveScore = rule.getLiveScore() != null ? rule.getLiveScore() : 0.0;
            double finalScore = (externalScore * 0.4) + (liveScore * 0.6);
            ruleRepository.updateExternalAndFinalScore(rule.getId(), externalScore, finalScore);
        }
    }

    // Also expose a manual trigger endpoint for testing
    public void triggerManualUpdate() {
        updateBenchmarkScores();
    }
}