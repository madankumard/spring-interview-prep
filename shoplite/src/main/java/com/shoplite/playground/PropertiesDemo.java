package com.shoplite.playground;

import com.shoplite.order.OrderProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PropertiesDemo implements CommandLineRunner {

    private final OrderProperties orderProperties;
    private final int maxItems;
    private final int bulkLimit;

    public PropertiesDemo(OrderProperties orderProperties,
                          @Value("${shoplite.orders.max-items-per-order}")int maxItems,
                          @Value("#{${shoplite.orders.max-items-per-order} * 2}") int bulkLimit) {
         this.orderProperties = orderProperties;
         this.maxItems = maxItems;
         this.bulkLimit = bulkLimit;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Record: " + orderProperties);
        System.out.println(">>> Payment timeout (default): " + orderProperties.paymentTimeout());
        System.out.println(">>> @Value max items: " + maxItems);
        System.out.println(">>> @Value SpEL bulk limit: " + bulkLimit);
    }
}
