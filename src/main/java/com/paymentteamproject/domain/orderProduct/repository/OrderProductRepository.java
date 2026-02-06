package com.paymentteamproject.domain.orderProduct.repository;

import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrder_User_Id(Long userId);

    List<OrderProduct> findAllByOrder_Id(Long orderId);
}
