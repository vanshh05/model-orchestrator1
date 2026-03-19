package com.yourcompany.orchestrator.core;

public class AIResponse {

    private final String content;
    private final String provider;
    private final String model;

    public AIResponse(String content,
                      String provider,
                      String model) {
        this.content = content;
        this.provider = provider;
        this.model = model;
    }

    public String getContent() { return content; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
}