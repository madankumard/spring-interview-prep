package com.shoplite.playground;

import com.shoplite.product.SkuGenerator;
import com.shoplite.tax.TaxCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class BeanStyleDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BeanStyleDemo.class);

    private final SkuGenerator skuGenerator;
    private final TaxCalculator taxCalculator;
    private final ApplicationContext context;

    public BeanStyleDemo(SkuGenerator skuGenerator, TaxCalculator taxCalculator, ApplicationContext context) {
        this.skuGenerator = skuGenerator;
        this.taxCalculator = taxCalculator;
        this.context = context;
    }

    @Override
    public void run(String... args) {
        log.info("SKU: {}", skuGenerator.next("keyboard"));
        log.info("Tax on 100.00 using {}: {}", taxCalculator.getClass().getSimpleName(),
                taxCalculator.tax(new BigDecimal("100.00")));

        log.info("@Component bean name: {}", Arrays.toString(context.getBeanNamesForType(SkuGenerator.class)));
        log.info("@Bean bean name:      {}", Arrays.toString(context.getBeanNamesForType(TaxCalculator.class)));
    }


}
