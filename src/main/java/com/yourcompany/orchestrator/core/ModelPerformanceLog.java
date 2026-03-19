package com.yourcompany.orchestrator.core;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "model_performance_log")
public class ModelPerformanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelName;
    private String providerName;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private Long responseTimeMs;
    private Boolean success;
    private Integer responseLength;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ModelPerformanceLog() {}

    public ModelPerformanceLog(String modelName, String providerName,
                               TaskType taskType, Long responseTimeMs,
                               Boolean success, Integer responseLength) {
        this.modelName = modelName;
        this.providerName = providerName;
        this.taskType = taskType;
        this.responseTimeMs = responseTimeMs;
        this.success = success;
        this.responseLength = responseLength;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getModelName() { return modelName; }
    public String getProviderName() { return providerName; }
    public TaskType getTaskType() { return taskType; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public Boolean getSuccess() { return success; }
    public Integer getResponseLength() { return responseLength; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}