package com.shoplite.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listProducts() {
        log.info("Listing products");
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        log.info("Fetching product {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product " + id + " not found"));
    }

    public Product createProduct(CreateProductRequest request) {
        Product saved = productRepository.save(new Product(null, request.name(), request.price()));
        log.info("Created product {}", saved.id());
        return saved;
    }

    public boolean deleteProduct(Long id) {
        boolean deleted = productRepository.deleteById(id);
        log.info("Delete product {} -> {}", id, deleted ? "deleted" : "not found");
        return deleted;
    }
}