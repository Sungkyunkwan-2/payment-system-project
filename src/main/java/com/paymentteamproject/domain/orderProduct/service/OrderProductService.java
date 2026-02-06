package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProductService {
    private final OrderProductRepository orderProductRepository;

    @Transactional(readOnly = true)
    public List<getAllOrderProductResponse> getAllOrderProducts() {
        List<OrderProduct> orderProducts = orderProductRepository.findAll();
        return orderProducts.stream()
                .map(orderProduct -> new getAllOrderProductResponse(
                        orderProduct.getOrder().getOrderNumber(),
                        orderProduct.getOrder().getId(),
                        orderProduct.getOrder().getTotalPrice(),
                        orderProduct.getOrder().getUsedPoint(),
                        orderProduct.getOrder().getUsedPoint(),  //TODO: 임의 테스트용
                        orderProduct.getOrder().getTotalPrice() - orderProduct.getOrder().getUsedPoint(),
                        orderProduct.getCurrency(),
                        orderProduct.getOrder().getStatus(),
                        orderProduct.getCreatedAt()
                ))
                .toList();
    }
}