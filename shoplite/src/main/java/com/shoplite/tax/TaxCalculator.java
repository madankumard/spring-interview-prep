package com.shoplite.tax;

import java.math.BigDecimal;

public interface TaxCalculator {
    BigDecimal tax(BigDecimal amount);
}
