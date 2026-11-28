package com.shoplite.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Product> getProduct(Long id) {
        log.info("Fetching product {}", id);
        return productRepository.findById(id);
    }

    public Product createProduct(CreateProductRequest request) {
        Product saved = productRepository.save(new Product(null, request.name(), request.price()));
        log.info("Created product {}", saved.id());
        return saved;
    }

    public Optional<Product> replaceProduct(Long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existing -> {
                    Product replaced = productRepository.save(new Product(id, request.name(), request.price()));
                    log.info("Replacing product {}", id);
                    return replaced;
                });
    }

    public Optional<Product> patchProduct(Long id, PatchProductRequest request) {
        return productRepository.findById(id)
                .map(existing -> {
                   Product patched = productRepository.save(
                           new Product(id,
                                   request.name() != null? request.name(): existing.name(),
                                   request.price() != null ? request.price(): existing.price()));
                   log.info("Patching product {}", id);
                   return patched;
                });
    }

    public boolean deleteProduct(Long id) {
        boolean deleted = productRepository.deleteById(id);
        log.info("Delete product {} -> {}", id, deleted ? "deleted" : "not found");
        return deleted;
    }
}