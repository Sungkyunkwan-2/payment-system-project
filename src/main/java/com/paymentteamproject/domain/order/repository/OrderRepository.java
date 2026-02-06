package com.paymentteamproject.domain.order.repository;

import com.paymentteamproject.domain.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
}
