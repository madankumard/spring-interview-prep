package com.shoplite.report;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class SalesReportGenerator {

    public SalesReportGenerator() {
        System.out.println(">>> SalesReportGenerator CREATED (expensive)");
    }

    public String generate() {
        return "Sales report: 42 orders today";
    }
}
