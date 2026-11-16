package com.shoplite.cart;

import com.shoplite.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
class ScopeDemo implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ScopeDemo.class);

    private final ApplicationContext context;
    private final CheckoutService checkoutService;

    ScopeDemo(ApplicationContext context, CheckoutService checkoutService) {
        this.context = context;
        this.checkoutService = checkoutService;
    }

    @Override
    public void run(String... args) {
        OrderService o1 = context.getBean(OrderService.class);
        OrderService o2 = context.getBean(OrderService.class);
        log.info("Singleton: same OrderService instance: {}", o1 == o2);

        ShoppingCart c1 = context.getBean(ShoppingCart.class);
        ShoppingCart c2 = context.getBean(ShoppingCart.class);
        log.info("Prototype: cart ids {} and {}, same instance: {}", c1.getId(), c2.getId(), c1 == c2);

        log.info("Pitfall: {}", checkoutService.addItem("alice", "Keyboard"));
        log.info("Pitfall: {}", checkoutService.addItem("bob", "Mouse"));
    }
}