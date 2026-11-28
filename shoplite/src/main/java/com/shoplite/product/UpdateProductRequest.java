package com.shoplite.product;

import java.math.BigDecimal;

public record UpdateProductRequest(String name, BigDecimal price) {
}
