package com.shoplite.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EuTaxCalculator implements TaxCalculator {

    @Override
    public BigDecimal tax(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
    }
}
