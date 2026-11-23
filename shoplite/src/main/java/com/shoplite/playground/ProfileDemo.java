package com.shoplite.playground;

import com.shoplite.payment.LoyaltyPointsService;
import com.shoplite.payment.PaymentGateway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class ProfileDemo implements CommandLineRunner {

    private final Environment env;
    private final PaymentGateway paymentGateway;
    private final ObjectProvider<LoyaltyPointsService> loyalty;

    public ProfileDemo(Environment env, PaymentGateway paymentGateway, ObjectProvider<LoyaltyPointsService> loyalty) {
        this.env = env;
        this.paymentGateway = paymentGateway;
        this.loyalty = loyalty;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println(">>> " + paymentGateway.charge("ORD-1", new BigDecimal("120.00")));
        loyalty.ifAvailable(service -> {
            System.out.println(">>> Loyalty points: " + service.pointsFor(new BigDecimal("120.00")));
        });
    }
}
