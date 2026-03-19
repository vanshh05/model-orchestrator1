package com.yourcompany.orchestrator.provider;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.yourcompany.orchestrator.core.AIRequest;
import com.yourcompany.orchestrator.core.AIResponse;
import com.yourcompany.orchestrator.exception.ProviderException;
import com.yourcompany.orchestrator.exception.ProviderRateLimitException;
import com.yourcompany.orchestrator.exception.ProviderUnavailableException;
import com.yourcompany.orchestrator.util.RetryHandler;

import jakarta.annotation.PostConstruct;

@Component
public class HuggingFaceProvider implements AIProvider {

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.base-url}")
    private String baseUrl;

    private RestClient restClient;
    private final RetryHandler retryHandler;

    public HuggingFaceProvider(RetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("x-wait-for-model", "true")
                .build();
    }

    @Override
    public String getName() {
        return "huggingface";
    }

    @Override
    public AIResponse generate(AIRequest request) {
        return retryHandler.executeWithRetry("huggingface", () -> callHuggingFace(request));
    }

    private AIResponse callHuggingFace(AIRequest request) {
        String modelId = request.getModelName();

        Map<String, Object> requestBody = Map.of(
            "model", modelId,
            "messages", List.of(
                Map.of("role", "user", "content", request.getPrompt())
            ),
            "max_tokens", 512
        );

        try {
            Map response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String generatedText = "No response from model.";
            if (response != null) {
                List<Map> choices = (List<Map>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map message = (Map) choices.get(0).get("message");
                    if (message != null) {
                        generatedText = message.get("content").toString();
                    }
                }
            }

            return new AIResponse(generatedText, "huggingface", modelId);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 429) {
                throw new ProviderRateLimitException("cerebras");
            } else if (e.getStatusCode().value() == 503) {
                throw new ProviderUnavailableException("huggingface");
            } else {
                throw new ProviderException("huggingface", body);
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            throw new ProviderUnavailableException("huggingface");
        } catch (Exception e) {
            throw new ProviderException("huggingface", e.getMessage());
        }
    }
}