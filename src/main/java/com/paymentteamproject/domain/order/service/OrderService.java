package com.paymentteamproject.domain.order.service;

import com.paymentteamproject.domain.orderProduct.exception.OrderProductEmptyException;
//import com.paymentteamproject.domain.pointTransactions.service.PointService;
import com.paymentteamproject.domain.product.exception.InsufficientStockException;
import com.paymentteamproject.domain.order.dto.CreateOrderRequest;
import com.paymentteamproject.domain.order.dto.CreateOrderResponse;
import com.paymentteamproject.domain.order.dto.OrderItemRequest;
import com.paymentteamproject.domain.order.entity.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import com.paymentteamproject.domain.product.entity.Product;
import com.paymentteamproject.domain.product.exception.ProductNotFoundException;
import com.paymentteamproject.domain.product.repository.ProductRepository;
import com.paymentteamproject.domain.user.entity.User;

import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    //private final PointService pointService;

    @Transactional
    public CreateOrderResponse createOrder(String email, CreateOrderRequest request) {
        // 주문 상품 목록 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderProductEmptyException("주문 상품이 비어있습니다.");
        }

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 총액 계산 및 상품 검증
        double totalAmount = 0.0;
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " + item.getProductId()));

            // 재고 확인
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("재고가 부족합니다. 상품: %s, 요청 수량: %d, 재고: %d",
                                product.getName(), item.getQuantity(), product.getStock())
                );
            }

            totalAmount += product.getPrice() * item.getQuantity();
        }

        // 주문 생성
        Orders order = Orders.builder()
                .user(user)
                .totalPrice(totalAmount)
                .usedPoint(0.0)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        Orders savedOrder = orderRepository.save(order);

        // 주문 상품 생성 및 재고 차감
        for (OrderItemRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));

            // 주문 상품 생성
            OrderProduct orderProduct = OrderProduct.builder()
                    .order(savedOrder)
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .currency("KRW") //통화는 원화로 고정
                    .quantity(item.getQuantity())
                    .build();

            orderProductRepository.save(orderProduct);

            // 재고 차감
            product.decreaseStock(item.getQuantity());
        }

        //포인트 적립
        //pointService.earnPoints(user, savedOrder);

        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderNumber()
        );
    }

    @Transactional
    public void processOrderCancellation(Orders order) {

        // 주문 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findByOrder(order);

        if (orderProducts.isEmpty()) {
            return;
        }

        // 각 상품의 재고 복구
        for (OrderProduct orderProduct : orderProducts) {
            Long productId = orderProduct.getProductId();
            Long quantity = orderProduct.getQuantity();

            // Product 조회
            Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("상품을 찾을 수 없습니다. productId: %d", productId)));

            // 재고 복구
            product.increaseStock(quantity);
        }
    }
}
