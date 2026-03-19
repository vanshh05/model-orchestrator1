package com.yourcompany.orchestrator.core;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "model_rule")
public class ModelRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private String providerName;

    private String modelName;

    private Integer priority;

    private Boolean active;

    private Double score;
    
    private Double externalScore;
    
    private Double liveScore;


    public ModelRule() {}

    public ModelRule(TaskType taskType,
                     String providerName,
                     String modelName,
                     Integer priority,
                     Boolean active) {
        this.taskType = taskType;
        this.providerName = providerName;
        this.modelName = modelName;
        this.priority = priority;
        this.active = active;
    }

    public Long getId() { return id; }
    public TaskType getTaskType() { return taskType; }
    public String getProviderName() { return providerName; }
    public String getModelName() { return modelName; }
    public Integer getPriority() { return priority; }
    public Boolean getActive() { return active; }
    public Double getScore() { return score; }
    public Double getExternalScore() { return externalScore != null ? externalScore : 0.0; }
    public Double getLiveScore() { return liveScore != null ? liveScore : 0.0; }
    


}