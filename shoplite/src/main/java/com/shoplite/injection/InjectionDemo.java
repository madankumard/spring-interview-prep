package com.shoplite.injection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InjectionDemo implements CommandLineRunner {

    private final FieldInjectedReceiptService fieldService;
    private final SetterInjectedReceiptService setterService;
    private final ConstructorInjectedReceiptService constructorService;

    public InjectionDemo(FieldInjectedReceiptService fieldService,
                         SetterInjectedReceiptService setterService,
                         ConstructorInjectedReceiptService constructorService) {
        this.fieldService = fieldService;
        this.setterService = setterService;
        this.constructorService = constructorService;
    }

    @Override
    public void run(String... args) {
        fieldService.sendReceipt("ann@shoplite.com", "A-1");
        setterService.sendReceipt("ann@shoplite.com", "A-2");
        constructorService.sendReceipt("ann@shoplite.com", "A-3");
    }
}
