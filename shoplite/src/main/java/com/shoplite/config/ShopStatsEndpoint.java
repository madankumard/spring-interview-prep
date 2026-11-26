package com.shoplite.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "shopstats")
public class ShopStatsEndpoint {

    private final Environment environment;
    private final Instant startedAt = Instant.now();

    public ShopStatsEndpoint(Environment environment) {
        this.environment = environment;
    }

    @ReadOperation
    public Map<String, Object> stats() {
        return Map.of("store", environment.getProperty("shoplite.store.name","ShopLite"),
                "activeProfiles", Arrays.asList(environment.getActiveProfiles()),
                "uptimeSeconds", Duration.between(startedAt, Instant.now()).toSeconds());
    }

}
