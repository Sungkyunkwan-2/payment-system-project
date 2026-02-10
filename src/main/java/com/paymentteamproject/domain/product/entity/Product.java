package com.paymentteamproject.domain.product.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.product.consts.ProductCategory;
import com.paymentteamproject.domain.product.consts.ProductStatus;
import com.paymentteamproject.domain.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private BigDecimal price;

    @Column (nullable = false)
    private Long stock;

    @Column (nullable = false)
    private String content;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column
    private LocalDateTime deletedAt;

    public Product(String name, BigDecimal price, Long stock, String content, ProductStatus status, ProductCategory category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.content = content;
        this.status = status;
        this.category = category;
    }

















































    // 재고 차감
    public void decreaseStock(Long quantity) {
        if (this.stock < quantity) {
            throw new InsufficientStockException(
                    String.format("재고가 부족합니다. 상품: %s, 요청 수량: %d, 현재 재고: %d",
                            this.name, quantity, this.stock)
            );
        }
        this.stock -= quantity;
    }

    //재고 증가
    public void increaseStock(Long quantity) {
        this.stock += quantity;
    }

}
