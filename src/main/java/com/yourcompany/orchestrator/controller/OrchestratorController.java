package com.yourcompany.orchestrator.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yourcompany.orchestrator.core.AIResponse;
import com.yourcompany.orchestrator.core.ModelPerformanceLog;
import com.yourcompany.orchestrator.core.ModelScoreResponse;
import com.yourcompany.orchestrator.core.OrchestrationRequest;
import com.yourcompany.orchestrator.exception.InvalidPromptException;
import com.yourcompany.orchestrator.repository.ModelPerformanceLogRepository;
import com.yourcompany.orchestrator.repository.ModelRuleRepository;
import com.yourcompany.orchestrator.service.BenchmarkUpdaterService;
import com.yourcompany.orchestrator.service.OrchestrationService;

@RestController
@RequestMapping("/api/v1/orchestrator")
public class OrchestratorController {

    private final OrchestrationService service;
    private final BenchmarkUpdaterService benchmarkUpdater;
    private final ModelRuleRepository modelRuleRepository;
    private final ModelPerformanceLogRepository performanceLogRepository;

    public OrchestratorController(OrchestrationService service,
                                   BenchmarkUpdaterService benchmarkUpdater,
                                   ModelRuleRepository modelRuleRepository,
                                   ModelPerformanceLogRepository performanceLogRepository) {
        this.service = service;
        this.benchmarkUpdater = benchmarkUpdater;
        this.modelRuleRepository = modelRuleRepository;
        this.performanceLogRepository = performanceLogRepository;
    }

    @PostMapping("/process")
    public AIResponse process(@RequestBody OrchestrationRequest request) {
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new InvalidPromptException("Prompt cannot be empty.");
        }
        return service.process(request.getPrompt());
    }

    @PostMapping("/refresh-scores")
    public String refreshScores() {
        benchmarkUpdater.triggerManualUpdate();
        return "Benchmark scores updated successfully!";
    }

    @GetMapping("/model-scores")
    public List<ModelScoreResponse> getModelScores() {
        return modelRuleRepository.findAll()
                .stream()
                .map(ModelScoreResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/history")
    public List<ModelPerformanceLog> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return performanceLogRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}