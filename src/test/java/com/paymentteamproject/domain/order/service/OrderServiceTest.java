package com.paymentteamproject.domain.order.service;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.order.dto.CreateOrderRequest;
import com.paymentteamproject.domain.order.dto.CreateOrderResponse;
import com.paymentteamproject.domain.order.dto.OrderItemRequest;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import com.paymentteamproject.domain.orderProduct.exception.OrderProductEmptyException;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.product.consts.ProductCategory;
import com.paymentteamproject.domain.product.consts.ProductStatus;
import com.paymentteamproject.domain.product.entity.Product;
import com.paymentteamproject.domain.product.exception.InsufficientStockException;
import com.paymentteamproject.domain.product.exception.ProductNotFoundException;
import com.paymentteamproject.domain.product.repository.ProductRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private OrderRepository orderRepository;
    @Mock private OrderProductRepository orderProductRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private PointService pointService;

    private User testUser;
    private Product testProduct;
    private OrderItemRequest testOrderItem;
    private CreateOrderRequest testOrderRequest;
    private Orders testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "홍길동",
                "010-1234-5678",
                "test@test.com",
                "password123",
                BigDecimal.ZERO
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testProduct = new Product(
                "테스트 상품",
                BigDecimal.valueOf(10000),
                10L,
                "상품 설명입니다.",
                ProductStatus.ONSALE,
                ProductCategory.FOOD
        );
        ReflectionTestUtils.setField(testProduct, "id", 100L);

        testOrderItem = new OrderItemRequest();
        ReflectionTestUtils.setField(testOrderItem, "productId", 100L);
        ReflectionTestUtils.setField(testOrderItem, "quantity", 2L);

        testOrderRequest = new CreateOrderRequest();
        ReflectionTestUtils.setField(testOrderRequest, "items", List.of(testOrderItem));

        testOrder = Orders.builder()
                .user(testUser)
                .orderNumber(20240101120000L)
                .totalPrice(BigDecimal.valueOf(20000))
                .usedPoint(BigDecimal.ZERO)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        ReflectionTestUtils.setField(testOrder, "id", 1L);
    }

    @Nested
    class 주문생성테스트 {

        @Test
        void 정상적인주문생성_성공() {
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(productRepository.findById(testProduct.getId()))
                    .thenReturn(Optional.of(testProduct));
            when(orderRepository.save(any(Orders.class)))
                    .thenReturn(testOrder);
            when(orderProductRepository.save(any(OrderProduct.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(pointService.createEarnPointsTransaction(any(User.class), any(Orders.class)))
                    .thenReturn(null);

            CreateOrderResponse response =
                    orderService.createOrder(testUser.getEmail(), testOrderRequest);

            assertThat(response.getOrderId()).isEqualTo(1L);
            assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.valueOf(20000));
            assertThat(response.getOrderNumber()).isEqualTo(20240101120000L);

            verify(userRepository).findByEmail(testUser.getEmail());
            verify(productRepository, times(2)).findById(testProduct.getId());
            verify(orderRepository).save(any(Orders.class));
            verify(orderProductRepository).save(any(OrderProduct.class));
            verify(pointService).createEarnPointsTransaction(testUser, testOrder);

            assertThat(testProduct.getStock()).isEqualTo(8L);
        }

        @Test
        void 주문생성_사용자없음() {
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.createOrder(testUser.getEmail(), testOrderRequest))
                    .isInstanceOf(UserNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        void 주문생성_상품목록비어있음() {
            CreateOrderRequest emptyRequest = new CreateOrderRequest();
            ReflectionTestUtils.setField(emptyRequest, "items", Collections.emptyList());

            assertThatThrownBy(() ->
                    orderService.createOrder(testUser.getEmail(), emptyRequest))
                    .isInstanceOf(OrderProductEmptyException.class);
        }

        @Test
        void 주문생성_상품없음() {
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(productRepository.findById(testProduct.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.createOrder(testUser.getEmail(), testOrderRequest))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void 주문생성_재고부족() {
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));

            Product lowStockProduct = new Product(
                    "재고 부족 상품",
                    BigDecimal.valueOf(10000),
                    1L,
                    "재고 부족 상품",
                    ProductStatus.ONSALE,
                    ProductCategory.FOOD
            );
            ReflectionTestUtils.setField(lowStockProduct, "id", 100L);

            when(productRepository.findById(testProduct.getId()))
                    .thenReturn(Optional.of(lowStockProduct));

            assertThatThrownBy(() ->
                    orderService.createOrder(testUser.getEmail(), testOrderRequest))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    class 주문취소테스트 {

        @Test
        void 주문취소_재고복구성공() {
            OrderProduct orderProduct = OrderProduct.builder()
                    .order(testOrder)
                    .productId(100L)
                    .productName("상품")
                    .price(BigDecimal.valueOf(10000))
                    .currency("KRW")
                    .quantity(2L)
                    .build();

            when(orderProductRepository.findByOrder(testOrder))
                    .thenReturn(List.of(orderProduct));
            when(productRepository.findByIdAndDeletedAtIsNull(100L))
                    .thenReturn(Optional.of(testProduct));

            orderService.processOrderCancellation(testOrder);

            verify(orderProductRepository).findByOrder(testOrder);
            verify(productRepository).findByIdAndDeletedAtIsNull(100L);

            assertThat(testProduct.getStock()).isEqualTo(12L);
        }

        @Test
        void 주문취소_주문상품없음() {
            when(orderProductRepository.findByOrder(testOrder))
                    .thenReturn(Collections.emptyList());

            orderService.processOrderCancellation(testOrder);

            verify(productRepository, never())
                    .findByIdAndDeletedAtIsNull(anyLong());
        }

        @Test
        void 주문취소_상품없음() {
            OrderProduct orderProduct = OrderProduct.builder()
                    .order(testOrder)
                    .productId(999L)
                    .productName("삭제상품")
                    .price(testProduct.getPrice())
                    .currency("KRW")
                    .quantity(2L)
                    .build();

            when(orderProductRepository.findByOrder(testOrder))
                    .thenReturn(List.of(orderProduct));
            when(productRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.processOrderCancellation(testOrder))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void 주문취소_여러상품재고복구() {
            Product mockProduct1 = mock(Product.class);
            Product mockProduct2 = mock(Product.class);

            OrderProduct op1 = OrderProduct.builder()
                    .order(testOrder)
                    .productId(100L)
                    .productName("상품1")
                    .price(BigDecimal.valueOf(10000))
                    .currency("KRW")
                    .quantity(2L)
                    .build();

            OrderProduct op2 = OrderProduct.builder()
                    .order(testOrder)
                    .productId(200L)
                    .productName("상품2")
                    .price(BigDecimal.valueOf(5000))
                    .currency("KRW")
                    .quantity(3L)
                    .build();

            when(orderProductRepository.findByOrder(testOrder))
                    .thenReturn(List.of(op1, op2));
            when(productRepository.findByIdAndDeletedAtIsNull(100L))
                    .thenReturn(Optional.of(mockProduct1));
            when(productRepository.findByIdAndDeletedAtIsNull(200L))
                    .thenReturn(Optional.of(mockProduct2));

            orderService.processOrderCancellation(testOrder);

            verify(mockProduct1).increaseStock(2L);
            verify(mockProduct2).increaseStock(3L);
        }
    }
}
