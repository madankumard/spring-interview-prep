package com.shoplite.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnBooleanProperty("shoplite.features.loyalty.enabled")
public class LoyaltyPointsService {

    public int pointsFor(BigDecimal amount) {
        return amount.intValue() / 10;
    }
}
