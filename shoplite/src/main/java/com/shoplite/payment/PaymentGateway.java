package com.shoplite.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    String charge(String orderId, BigDecimal amount);
}
