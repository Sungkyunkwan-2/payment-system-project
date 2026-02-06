package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProductService {
    private final OrderProductRepository orderProductRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<getAllOrderProductResponse> getAllOrderProducts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<OrderProduct> orderProducts = orderProductRepository.findAll();
        return orderProducts.stream()
                .map(orderProduct -> new getAllOrderProductResponse(
                        orderProduct.getOrder().getOrderNumber(),
                        orderProduct.getOrder().getId(),
                        orderProduct.getOrder().getTotalPrice(), //포인트 차감 전 총 주문금액
                        orderProduct.getOrder().getUsedPoint(),  //유저가 사용할 포인트
                        orderProduct.getOrder().getUsedPoint(),  //TODO: 임의 테스트용 (적립되는 포인트)
                        orderProduct.getOrder().getTotalPrice() - orderProduct.getOrder().getUsedPoint(),  //포인트 차감 후 총 주문금액
                        orderProduct.getCurrency(),  //통화
                        orderProduct.getOrder().getStatus(),  //주문 상태
                        orderProduct.getCreatedAt()
                ))
                .toList();
    }
}