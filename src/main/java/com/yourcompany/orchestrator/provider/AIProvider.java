package com.yourcompany.orchestrator.provider;

import com.yourcompany.orchestrator.core.AIRequest;
import com.yourcompany.orchestrator.core.AIResponse;

public interface AIProvider {

    String getName();

    AIResponse generate(AIRequest request);
}