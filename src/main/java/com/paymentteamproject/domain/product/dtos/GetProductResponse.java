package com.paymentteamproject.domain.product.dtos;

import com.paymentteamproject.domain.product.entity.ProductCategory;
import com.paymentteamproject.domain.product.entity.ProductStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetProductResponse {
    private final Long id;
    private final String name;
    private final double price;
    private final Long stock;
    private final String content;
    private final ProductStatus status;
    private final ProductCategory category;
    private final LocalDateTime createdAt;

    public GetProductResponse(Long id, String name, double price, Long stock, String content, ProductStatus status, ProductCategory category, LocalDateTime createdAt) {
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
