package com.yourcompany.orchestrator.routing;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yourcompany.orchestrator.provider.AIProvider;
import com.yourcompany.orchestrator.core.AIRequest;
import com.yourcompany.orchestrator.core.AIResponse;

@Component
public class ModelRouter {

    private final Map<String, AIProvider> providers;

    public ModelRouter(List<AIProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(AIProvider::getName, p -> p));
    }

    public AIResponse route(String providerName,
                            String modelName,
                            String prompt) {

        AIProvider provider = providers.get(providerName);

        if (provider == null) {
            throw new RuntimeException("Provider not found: " + providerName);
        }

        AIRequest request = new AIRequest(modelName, prompt);

        return provider.generate(request);
    }
}