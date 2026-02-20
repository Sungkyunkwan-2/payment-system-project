package com.paymentteamproject.domain.product.dto;

import com.paymentteamproject.domain.product.consts.ProductCategory;
import com.paymentteamproject.domain.product.consts.ProductStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetProductResponse {
    private final Long id;
    private final String name;
    private final BigDecimal price;
    private final Long stock;
    private final String content;
    private final ProductStatus status;
    private final ProductCategory category;
    private final LocalDateTime createdAt;

    public GetProductResponse(Long id, String name, BigDecimal price, Long stock, String content, ProductStatus status, ProductCategory category, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.content = content;
        this.status = status;
        this.category = category;
        this.createdAt = createdAt;
    }
}
