package com.shoplite.config;

import com.shoplite.tax.EuTaxCalculator;
import com.shoplite.tax.TaxCalculator;
import com.shoplite.tax.UsTaxCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    Clock clock(){
      return Clock.systemUTC();
    }

    @Bean
    DateTimeFormatter orderDateFormatter(){
        return DateTimeFormatter.ofPattern("dd MMM yyyy");
    }

    @Bean
    DateTimeFormatter isoDateFormatter(){
        return DateTimeFormatter.ISO_LOCAL_DATE;
    }

    @Bean
    TaxCalculator taxCalculator(@Value("${shoplite.tax.region:EU}") String region){
        log.info("Creating TaxCalculator for region {}", region);
        return region.equals("US") ? new UsTaxCalculator() : new EuTaxCalculator();
    }
}
