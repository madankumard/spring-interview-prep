package com.shoplite.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final boolean gatewayUp;

    public PaymentGatewayHealthIndicator(@Value("${shoplite.payment.gateway-up:true}") boolean gatewayUp) {
        this.gatewayUp = gatewayUp;
    }

    @Override
    public Health health() {
        if(gatewayUp) {
            return Health.up()
                    .withDetail("provider","FakePay")
                    .withDetail("latencyMs",42)
                    .build();
        }

        return Health.down()
                .withDetail("provider","FakePay")
                .withDetail("error","Connection refused")
                .build();
    }
}
