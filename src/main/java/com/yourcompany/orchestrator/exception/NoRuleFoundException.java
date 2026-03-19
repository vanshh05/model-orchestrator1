package com.yourcompany.orchestrator.exception;

import com.yourcompany.orchestrator.core.TaskType;

public class NoRuleFoundException extends RuntimeException {
    public NoRuleFoundException(TaskType taskType) {
        super("No active rule found for task type: " + taskType);
    }
}