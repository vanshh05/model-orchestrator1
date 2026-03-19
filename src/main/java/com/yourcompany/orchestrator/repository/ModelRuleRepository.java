package com.yourcompany.orchestrator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.yourcompany.orchestrator.core.ModelRule;
import com.yourcompany.orchestrator.core.TaskType;

public interface ModelRuleRepository extends JpaRepository<ModelRule, Long> {

    Optional<ModelRule> findFirstByTaskTypeAndActiveTrueOrderByPriorityAsc(TaskType taskType);

    Optional<ModelRule> findFirstByTaskTypeAndActiveTrueOrderByScoreDesc(TaskType taskType);

    @Modifying
    @Transactional
    @Query("UPDATE ModelRule r SET r.liveScore = :liveScore, r.score = :finalScore WHERE r.id = :id")
    void updateScores(@Param("id") Long id,
                      @Param("liveScore") Double liveScore,
                      @Param("finalScore") Double finalScore);
    
                      @Modifying
@Transactional
@Query("UPDATE ModelRule r SET r.externalScore = :externalScore, r.score = :finalScore WHERE r.id = :id")
void updateExternalAndFinalScore(@Param("id") Long id,
                                  @Param("externalScore") Double externalScore,
                                  @Param("finalScore") Double finalScore);
}