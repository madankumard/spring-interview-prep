package com.shoplite.playground;

import com.shoplite.product.ProductController;
import com.shoplite.product.ProductRepository;
import com.shoplite.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StereotypeInspector implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StereotypeInspector.class);

    @Override
    public void run(String... args) {
        for (Class<?> type : List.of(ProductController.class, ProductService.class, ProductRepository.class)) {
            boolean isComponent = MergedAnnotations.from(type).isPresent(Component.class);
            log.info("{} is a @Component underneath: {}", type.getSimpleName(), isComponent);
        }
    }
}