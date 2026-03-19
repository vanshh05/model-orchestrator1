package com.yourcompany.orchestrator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yourcompany.orchestrator.core.ModelPerformanceLog;
import com.yourcompany.orchestrator.core.TaskType;

public interface ModelPerformanceLogRepository extends JpaRepository<ModelPerformanceLog, Long> {

    @Query("""
        SELECT AVG(CASE WHEN l.success = true THEN 1.0 ELSE 0.0 END) * 50
             + (1.0 - LEAST(AVG(l.responseTimeMs) / 10000.0, 1.0)) * 30
             + LEAST(AVG(l.responseLength) / 500.0, 1.0) * 20
        FROM ModelPerformanceLog l
        WHERE l.modelName = :modelName AND l.taskType = :taskType
    """)
    Double calculateLiveScore(@Param("modelName") String modelName,
                              @Param("taskType") TaskType taskType);
}