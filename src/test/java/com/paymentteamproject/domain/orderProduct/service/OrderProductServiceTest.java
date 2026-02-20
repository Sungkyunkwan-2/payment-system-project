package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.exception.OrderAccessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProductServiceTest {

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private OrderProductService orderProductService;

    private User user;
    private Orders order;
    private OrderProduct orderProduct;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .username("홍길동")
                .phone("01012345678")
                .email("test@test.com")
                .password("password")
                .pointBalance(BigDecimal.valueOf(1000))
                .build();

        order = Orders.builder()
                .user(user)
                .orderNumber(202502130001L)
                .totalPrice(BigDecimal.valueOf(10000))
                .usedPoint(BigDecimal.valueOf(1000))
                .status(OrderStatus.ORDER_COMPLETED)
                .build();

        orderProduct = OrderProduct.builder()
                .order(order)
                .productId(1L)
                .productName("상품1")
                .price(BigDecimal.valueOf(10000))
                .currency("KRW")
                .quantity(1L)
                .build();
    }

    @Test
    void 전체주문상품조회_성공() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderProductRepository.findAllByOrderUser(user))
                .thenReturn(List.of(orderProduct));

        when(pointTransactionRepository.findEarnedPointsByOrderId(any()))
                .thenReturn(BigDecimal.valueOf(500));

        List<getAllOrderProductResponse> result =
                orderProductService.getAllOrderProducts(user.getEmail());

        assertThat(result).hasSize(1);

        getAllOrderProductResponse response = result.get(0);

        assertThat(response.getOrderNumber()).isEqualTo(202502130001L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("10000");
        assertThat(response.getUserPoints()).isEqualByComparingTo("1000");
        assertThat(response.getFinalAmount()).isEqualByComparingTo("9000");
        assertThat(response.getEarnedPoints()).isEqualByComparingTo("500");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDER_COMPLETED);
        assertThat(response.getCurrency()).isEqualTo("KRW");

        verify(userRepository).findByEmail(user.getEmail());
        verify(orderProductRepository).findAllByOrderUser(user);
        verify(pointTransactionRepository).findEarnedPointsByOrderId(any());
    }

    @Test
    void 전체주문상품조회_사용자없음() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderProductService.getAllOrderProducts(user.getEmail()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 단일주문조회_성공() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderProductRepository.findAllByOrderId(1L))
                .thenReturn(List.of(orderProduct));

        when(pointTransactionRepository.findEarnedPointsByOrderId(1L))
                .thenReturn(BigDecimal.valueOf(500));

        getOneOrderProductResponse response =
                orderProductService.getOneOrderProducts(user.getEmail(), 1L);

        assertThat(response.getOrderNumber()).isEqualTo(202502130001L);
        assertThat(response.getFinalAmount()).isEqualByComparingTo("9000");
        assertThat(response.getEarnedPoints()).isEqualByComparingTo("500");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDER_COMPLETED);
    }

    @Test
    void 단일주문조회_주문없음() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderProductService.getOneOrderProducts(user.getEmail(), 1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void 단일주문조회_본인주문아님() {

        User otherUser = User.builder()
                .username("다른사용자")
                .phone("01099999999")
                .email("other@test.com")
                .password("pw")
                .pointBalance(BigDecimal.ZERO)
                .build();

        Orders otherOrder = Orders.builder()
                .user(otherUser)
                .orderNumber(999L)
                .totalPrice(BigDecimal.valueOf(5000))
                .usedPoint(BigDecimal.ZERO)
                .status(OrderStatus.ORDER_COMPLETED)
                .build();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(2L))
                .thenReturn(Optional.of(otherOrder));

        assertThatThrownBy(() ->
                orderProductService.getOneOrderProducts(user.getEmail(), 2L))
                .isInstanceOf(OrderAccessException.class);
    }
}
