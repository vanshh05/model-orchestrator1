package com.yourcompany.orchestrator.core;

public class ModelScoreResponse {

    private String taskType;
    private String modelName;
    private String providerName;
    private Double externalScore;
    private Double liveScore;
    private Double finalScore;
    private Boolean active;

    public ModelScoreResponse(ModelRule rule) {
        this.taskType = rule.getTaskType().name();
        this.modelName = rule.getModelName();
        this.providerName = rule.getProviderName();
        this.externalScore = rule.getExternalScore();
        this.liveScore = rule.getLiveScore();
        this.finalScore = rule.getScore();
        this.active = rule.getActive();
    }

    public String getTaskType() { return taskType; }
    public String getModelName() { return modelName; }
    public String getProviderName() { return providerName; }
    public Double getExternalScore() { return externalScore; }
    public Double getLiveScore() { return liveScore; }
    public Double getFinalScore() { return finalScore; }
    public Boolean getActive() { return active; }
}