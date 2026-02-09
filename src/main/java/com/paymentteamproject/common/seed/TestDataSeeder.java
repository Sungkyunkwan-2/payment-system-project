package com.paymentteamproject.common.seed;

import com.paymentteamproject.domain.product.entity.Product;
import com.paymentteamproject.domain.product.entity.ProductCategory;
import com.paymentteamproject.domain.product.entity.ProductStatus;
import com.paymentteamproject.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {

        Product product1 = new Product(
                "피자",
                1000.0,
                10L,
                "테스트 상품",
                ProductStatus.ONSALE,
                ProductCategory.FOOD
        );

        Product product2 = new Product(
                "콜라",
                1000.0,
                50L,
                "테스트 상품",
                ProductStatus.ONSALE,
                ProductCategory.FOOD
        );

        productRepository.save(product1);
        productRepository.save(product2);

        System.out.println("✅ 상품 더미 데이터 생성 완료");
    }
}