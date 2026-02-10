package com.paymentteamproject.domain.orderProduct.repository;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrderUserId(Long userId);

    List<OrderProduct> findAllByOrderId(Long orderId);

    List<OrderProduct> findAllByOrderUserEmail(String email);

    List<OrderProduct> findByOrder(Orders order);
}
