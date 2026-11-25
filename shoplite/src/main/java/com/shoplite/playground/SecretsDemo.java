package com.shoplite.playground;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SecretsDemo implements CommandLineRunner {

    private final String apiKey;
    private final String webhookSecret;

    public SecretsDemo(@Value("${shoplite.payment.api-key:<not set>}") String apiKey,
                       @Value("${shoplite.payment.webhook-secret:<not set>}") String webhookSecret) {
        this.apiKey = apiKey;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("### SECRET: api-key        = " + mask(apiKey));
        System.out.println("### SECRET: webhook-secret = " + mask(webhookSecret));
    }

    private String mask(String value) {
        if (value.startsWith("<")) {
            return value;
        }
        return value.substring(0, Math.min(4, value.length())) + "**** (length " + value.length() + ")";
    }
}
