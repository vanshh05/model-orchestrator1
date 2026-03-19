package com.yourcompany.orchestrator.service;

import org.springframework.stereotype.Service;

import com.yourcompany.orchestrator.core.AIResponse;
import com.yourcompany.orchestrator.core.ModelPerformanceLog;
import com.yourcompany.orchestrator.core.ModelRule;
import com.yourcompany.orchestrator.core.TaskType;
import com.yourcompany.orchestrator.repository.ModelPerformanceLogRepository;
import com.yourcompany.orchestrator.repository.ModelRuleRepository;

@Service
public class PerformanceTrackerService {

    private final ModelPerformanceLogRepository logRepository;
    private final ModelRuleRepository ruleRepository;

    public PerformanceTrackerService(ModelPerformanceLogRepository logRepository,
                                     ModelRuleRepository ruleRepository) {
        this.logRepository = logRepository;
        this.ruleRepository = ruleRepository;
    }

    public void track(ModelRule rule, TaskType taskType,
                      AIResponse response, long responseTimeMs, boolean success) {

        // 1. Save raw log entry
        logRepository.save(new ModelPerformanceLog(
                rule.getModelName(),
                rule.getProviderName(),
                taskType,
                responseTimeMs,
                success,
                success ? response.getContent().length() : 0
        ));

        // 2. Recalculate live score from all logs for this model+task
        Double liveScore = logRepository.calculateLiveScore(rule.getModelName(), taskType);
        if (liveScore == null) liveScore = 0.0;

        // 3. Recalculate final hybrid score: 40% external + 60% live
        // When live_score is 0 (no data yet), use full external score
// As live data builds up, it transitions to the 40/60 hybrid
double finalScore;
if (liveScore == 0.0) {
    finalScore = rule.getExternalScore();
} else {
    finalScore = (rule.getExternalScore() * 0.4) + (liveScore * 0.6);
}
        // 4. Update model_rule with new scores
        ruleRepository.updateScores(rule.getId(), liveScore, finalScore);
    }
}