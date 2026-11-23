package com.shoplite.playground;

import com.shoplite.report.ReportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LazyDemo implements CommandLineRunner {

    private final ReportService reportService;

    public LazyDemo(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> Before first call");
        System.out.println(">>> " + reportService.dailyReport());
        System.out.println(">>> " + reportService.dailyReport());
    }
}
