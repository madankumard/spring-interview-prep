package com.shoplite.product;

import java.math.BigDecimal;

public record PatchProductRequest(String name, BigDecimal price) {
}
