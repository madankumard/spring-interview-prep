package com.shoplite.report;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final SalesReportGenerator generator;

    public ReportService(@Lazy SalesReportGenerator generator) {
        this.generator = generator;
        System.out.println(">>> ReportService created, got: " +
                generator.getClass().getSimpleName());
    }

    public String dailyReport() {
        return generator.generate();
    }
}
