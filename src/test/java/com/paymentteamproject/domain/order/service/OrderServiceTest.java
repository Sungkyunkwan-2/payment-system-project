//package com.paymentteamproject.domain.order.service;
//
//import com.paymentteamproject.domain.order.dto.CreateOrderRequest;
//import com.paymentteamproject.domain.order.dto.CreateOrderResponse;
//import com.paymentteamproject.domain.order.dto.OrderItemRequest;
//import com.paymentteamproject.domain.order.entity.OrderStatus;
//import com.paymentteamproject.domain.order.entity.Orders;
//import com.paymentteamproject.domain.order.repository.OrderRepository;
//import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
//import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
//import com.paymentteamproject.domain.product.entity.Product;
//import com.paymentteamproject.domain.product.entity.ProductCategory;
//import com.paymentteamproject.domain.product.entity.ProductStatus;
//import com.paymentteamproject.domain.product.repository.ProductRepository;
//import com.paymentteamproject.domain.user.entity.User;
//import com.paymentteamproject.domain.user.repository.UserRepository;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @InjectMocks
//    private OrderService orderService;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OrderProductRepository orderProductRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Test
//    void createOrder_정상_주문_생성() {
//        // given
//        Long userId = 1L;
//
//        User user = new User(
//                "홍길동",
//                "010-1234-5678",
//                "test@test.com",
//                "password",
//                0.0
//        );
//        ReflectionTestUtils.setField(user, "id", userId);
//
//        Product product = new Product(
//                "테스트 상품",
//                10_000.0,
//                10L,
//                "상품 설명",
//                ProductStatus.ONSALE,
//                ProductCategory.FOOD
//        );
//        ReflectionTestUtils.setField(product, "id", 100L);
//
//        // OrderItemRequest 생성
//        OrderItemRequest itemRequest = new OrderItemRequest();
//        ReflectionTestUtils.setField(itemRequest, "productId", product.getId());
//        ReflectionTestUtils.setField(itemRequest, "quantity", 2L);
//
//        // CreateOrderRequest 생성
//        CreateOrderRequest request = new CreateOrderRequest();
//        ReflectionTestUtils.setField(request, "items", List.of(itemRequest));
//
//        Orders savedOrder = Orders.builder()
//                .user(user)
//                .orderNumber(1234567890L)
//                .totalPrice(20_000.0)
//                .usedPoint(0.0)
//                .status(OrderStatus.PAYMENT_PENDING)
//                .build();
//
//        ReflectionTestUtils.setField(savedOrder, "id", 1L);
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
//        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
//        when(orderProductRepository.save(any(OrderProduct.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        CreateOrderResponse response = orderService.createOrder(userId, request);
//
//        // then
//        assertThat(response.getOrderId()).isEqualTo(1L);
//        assertThat(response.getTotalAmount()).isEqualTo(20_000.0);
//        assertThat(response.getOrderNumber()).isEqualTo(1234567890L);
//
//        verify(orderRepository).save(any(Orders.class));
//        verify(orderProductRepository).save(any(OrderProduct.class));
//    }
//}
