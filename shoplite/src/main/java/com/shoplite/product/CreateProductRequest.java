package com.shoplite.product;

import java.math.BigDecimal;

public record CreateProductRequest(String name, BigDecimal price) {
}
