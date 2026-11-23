package com.shoplite.payment;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class CardPaymentGateway implements PaymentGateway {

    @Override
    public String charge(String orderId, BigDecimal amount) {
        return "CARD charge " + amount + " for " + orderId;
    }
}