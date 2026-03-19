package com.yourcompany.orchestrator.core;

public class ModelConfig {

    private final String provider;
    private final String modelName;

    public ModelConfig(String provider, String modelName) {
        this.provider = provider;
        this.modelName = modelName;
    }

    public String getProvider() {
        return provider;
    }

    public String getModelName() {
        return modelName;
    }
}