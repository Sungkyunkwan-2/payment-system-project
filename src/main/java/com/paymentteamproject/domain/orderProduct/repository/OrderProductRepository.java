package com.paymentteamproject.domain.orderProduct.repository;

import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
