package com.shoplite;

import com.shoplite.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanInspector implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BeanInspector.class);

    private final ApplicationContext context;

    public BeanInspector(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Total beans in container: {}", context.getBeanDefinitionCount());
        log.info("'orderService' is a bean: {}", context.containsBean("orderService"));
        log.info("'clock' is a bean: {}", context.containsBean("clock"));

        OrderService a = context.getBean(OrderService.class);
        OrderService b = context.getBean(OrderService.class);
        log.info("Same instance (singleton): {}", a == b);

        log.info("Clock bean: {}", context.getBean("clock"));
    }
}
