package com.shoplite.order;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("shoplite.orders")
public record OrderProperties(
        @Min(1) int maxItemsPerOrder,
        @NotNull @DecimalMin("0.0") BigDecimal freeShippingThreshold,
        @DefaultValue("30s") Duration paymentTimeout,
        @NotEmpty List<String> allowedCountries) {
}
