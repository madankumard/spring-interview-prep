package com.shoplite.payment;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Profile("!prod")
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public String charge(String orderId, BigDecimal amount) {
        return "FAKE charge " + amount + " for " + orderId;
    }
}
