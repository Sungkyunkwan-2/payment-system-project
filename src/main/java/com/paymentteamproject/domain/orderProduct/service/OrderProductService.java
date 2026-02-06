package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.common.exception.ForbiddenException;
import com.paymentteamproject.common.exception.ResourceNotFoundException;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.dto.getOneOrderProductResponse;
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
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<getAllOrderProductResponse> getAllOrderProducts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        List<OrderProduct> orderProducts =
                orderProductRepository.findAllByOrder_User_Id(userId);
        return orderProducts.stream()
                .map(orderProduct -> new getAllOrderProductResponse(
                        orderProduct.getOrder().getOrderNumber(),
                        orderProduct.getOrder().getId(),
                        orderProduct.getOrder().getTotalPrice(), //포인트 차감 전 총 주문금액
                        orderProduct.getOrder().getUsedPoint(),  //유저가 사용할 포인트
                        orderProduct.getOrder().getTotalPrice() - orderProduct.getOrder().getUsedPoint(),  //포인트 차감 후 총 주문금액
                        orderProduct.getOrder().getUsedPoint(),  //TODO: 임의 테스트용 (적립되는 포인트)
                        orderProduct.getCurrency(),
                        orderProduct.getOrder().getStatus(),
                        orderProduct.getCreatedAt()
                ))
                .toList();
    }

    public getOneOrderProductResponse getOneOrderProducts(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인의 주문만 조회할 수 있습니다.");
        }

        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder_Id(orderId);

        OrderProduct orderProduct = orderProducts.get(0);

        return new getOneOrderProductResponse(
                order.getOrderNumber(),
                order.getId(),
                order.getTotalPrice(),
                order.getUsedPoint(),
                order.getTotalPrice() - order.getUsedPoint(),
                order.getUsedPoint(), // 임시 적립 포인트
                orderProduct.getCurrency(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}