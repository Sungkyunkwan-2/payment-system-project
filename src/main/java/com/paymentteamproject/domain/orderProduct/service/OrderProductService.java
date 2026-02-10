package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.domain.order.exception.OrderAccessException;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.exception.OrderNotFoundException;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.dto.getOneOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import com.paymentteamproject.domain.pointTransaction.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
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
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public List<getAllOrderProductResponse> getAllOrderProducts(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<OrderProduct> orderProducts =
                orderProductRepository.findAllByOrderUser(user);


        return orderProducts.stream()
                .map(orderProduct -> {
                    Orders order = orderProduct.getOrder();
                    // 해당 주문으로 적립된 포인트 조회
                    Double earnedPoints = pointTransactionRepository.findEarnedPointsByOrderId(order.getId());

                    return new getAllOrderProductResponse(
                            order.getOrderNumber(),
                            order.getId(),
                            order.getTotalPrice(),
                            order.getUsedPoint(),
                            order.getTotalPrice() - order.getUsedPoint(),
                            earnedPoints,  // 실제 적립된 포인트
                            orderProduct.getCurrency(),
                            order.getStatus(),
                            orderProduct.getCreatedAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public getOneOrderProductResponse getOneOrderProducts(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        if (!order.getUser().equals(user)) {
            throw new OrderAccessException("본인의 주문만 조회할 수 있습니다.");
        }

        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderId(orderId);

        OrderProduct orderProduct = orderProducts.get(0);

        Double earnedPoints = pointTransactionRepository.findEarnedPointsByOrderId(orderId);
        return new getOneOrderProductResponse(
                order.getOrderNumber(),
                order.getId(),
                order.getTotalPrice(),
                order.getUsedPoint(),
                order.getTotalPrice() - order.getUsedPoint(),
                earnedPoints,
                orderProduct.getCurrency(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}