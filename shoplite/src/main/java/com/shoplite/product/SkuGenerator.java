package com.shoplite.product;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SkuGenerator {
    private final Clock clock;
    private final DateTimeFormatter formatter;
    private final AtomicInteger sequence = new AtomicInteger();


    public SkuGenerator(Clock clock, @Qualifier("orderDateFormatter") DateTimeFormatter formatter) {
        this.clock = clock;
        this.formatter = formatter;
    }

    public String next(String productName) {
        return productName.toUpperCase() + "-" + sequence.incrementAndGet()
                + " (" + LocalDate.now(clock).format(formatter) + ")";
    }
}
