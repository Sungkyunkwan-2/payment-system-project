package com.paymentteamproject.domain.product.repository;

import com.paymentteamproject.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
