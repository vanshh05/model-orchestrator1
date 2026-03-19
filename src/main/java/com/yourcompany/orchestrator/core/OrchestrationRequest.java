package com.yourcompany.orchestrator.core;

public class OrchestrationRequest {

    private String prompt;
    private String userId;
    private String context;

    public OrchestrationRequest() {}

    public String getPrompt() { return prompt; }
    public String getUserId() { return userId; }
    public String getContext() { return context; }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setContext(String context) { this.context = context; }
}