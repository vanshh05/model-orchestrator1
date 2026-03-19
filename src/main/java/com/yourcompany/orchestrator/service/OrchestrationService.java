package com.yourcompany.orchestrator.service;

import org.springframework.stereotype.Service;

import com.yourcompany.orchestrator.analyzer.PromptAnalyzer;
import com.yourcompany.orchestrator.core.AIResponse;
import com.yourcompany.orchestrator.core.ModelRule;
import com.yourcompany.orchestrator.core.TaskType;
import com.yourcompany.orchestrator.routing.ModelRouter;

@Service
public class OrchestrationService {

    private final PromptAnalyzer analyzer;
    private final RuleEngine ruleEngine;
    private final ModelRouter router;
    private final PerformanceTrackerService tracker;

    public OrchestrationService(PromptAnalyzer analyzer, RuleEngine ruleEngine,
                                ModelRouter router, PerformanceTrackerService tracker) {
        this.analyzer = analyzer;
        this.ruleEngine = ruleEngine;
        this.router = router;
        this.tracker = tracker;
    }

    public AIResponse process(String prompt) {

    TaskType task = analyzer.analyze(prompt);
    ModelRule rule = ruleEngine.resolve(task);

    long startTime = System.currentTimeMillis();
    AIResponse response;
    boolean success = true;

    try {
        response = router.route(rule.getProviderName(), rule.getModelName(), prompt);
    } catch (Exception e) {
        success = false;
        long responseTimeMs = System.currentTimeMillis() - startTime;
        // Still track the failed request for live score
        tracker.track(rule, task,
                new AIResponse("", rule.getProviderName(), rule.getModelName()),
                responseTimeMs, false);
        throw e; // Re-throw so GlobalExceptionHandler returns proper HTTP code
    }

    long responseTimeMs = System.currentTimeMillis() - startTime;
    tracker.track(rule, task, response, responseTimeMs, success);
    return response;
    }
}