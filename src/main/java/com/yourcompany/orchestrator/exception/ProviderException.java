package com.yourcompany.orchestrator.exception;

public class ProviderException extends RuntimeException {
    public ProviderException(String provider, String detail) {
        super("Provider '" + provider + "' returned an error: " + detail);
    }
} 