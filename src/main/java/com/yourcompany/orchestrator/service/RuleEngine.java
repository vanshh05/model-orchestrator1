package com.yourcompany.orchestrator.service;

import org.springframework.stereotype.Service;

import com.yourcompany.orchestrator.core.ModelRule;
import com.yourcompany.orchestrator.core.TaskType;
import com.yourcompany.orchestrator.exception.NoRuleFoundException;
import com.yourcompany.orchestrator.repository.ModelRuleRepository;

@Service
public class RuleEngine {

    private final ModelRuleRepository repository;

    public RuleEngine(ModelRuleRepository repository) {
        this.repository = repository;
    }

    public ModelRule resolve(TaskType taskType) {
        return repository
                .findFirstByTaskTypeAndActiveTrueOrderByScoreDesc(taskType)
                .orElseThrow(() -> new NoRuleFoundException(taskType));
    }
}