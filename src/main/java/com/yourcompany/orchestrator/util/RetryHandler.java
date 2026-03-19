package com.yourcompany.orchestrator.util;

import org.springframework.stereotype.Component;

import com.yourcompany.orchestrator.exception.ProviderRateLimitException;

@Component
public class RetryHandler {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000;

    public <T> T executeWithRetry(String providerName, RetryableOperation<T> operation) {
        int attempt = 0;
        long delay = INITIAL_DELAY_MS;

        while (attempt < MAX_RETRIES) {
            try {
                return operation.execute();
            } catch (ProviderRateLimitException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    System.err.println("[RetryHandler] Max retries reached for " + providerName);
                    throw e;
                }
                System.out.println("[RetryHandler] Rate limited. Retrying in " + delay + "ms (attempt " + attempt + "/" + MAX_RETRIES + ")");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                delay *= 2;
            }
        }
        throw new ProviderRateLimitException(providerName);
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute();
    }
}