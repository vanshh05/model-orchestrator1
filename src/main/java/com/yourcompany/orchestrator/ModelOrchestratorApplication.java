package com.yourcompany.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModelOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelOrchestratorApplication.class, args);
    }
}