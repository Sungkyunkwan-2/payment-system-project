package com.paymentteamproject.domain.orderProduct.repository;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrderId(Long orderId);

    List<OrderProduct> findByOrder(Orders order);

    List<OrderProduct> findAllByOrderUser(User user);
}
