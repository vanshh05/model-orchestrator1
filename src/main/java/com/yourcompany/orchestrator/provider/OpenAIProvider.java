package com.yourcompany.orchestrator.provider;

import org.springframework.stereotype.Component;

import com.yourcompany.orchestrator.core.AIRequest;
import com.yourcompany.orchestrator.core.AIResponse;

@Component
public class OpenAIProvider implements AIProvider {

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public AIResponse generate(AIRequest request) {

        return new AIResponse(
                "Mock response from OpenAI for model: "
                        + request.getModelName(),
                "openai",
                request.getModelName()
        );
    }
}