package com.yourcompany.orchestrator.core;

public class AIRequest {

    private final String modelName;
    private final String prompt;

    public AIRequest(String modelName, String prompt) {
        this.modelName = modelName;
        this.prompt = prompt;
    }

    public String getModelName() { return modelName; }
    public String getPrompt() { return prompt; }
}