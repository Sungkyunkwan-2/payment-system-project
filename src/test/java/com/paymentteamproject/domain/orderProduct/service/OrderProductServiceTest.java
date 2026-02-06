package com.paymentteamproject.domain.orderProduct.service;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.dto.getOneOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.orderProduct.repository.OrderProductRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProductServiceTest {

    @InjectMocks
    private OrderProductService orderProductService;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void getAllOrderProducts_성공() {
        // given
        Long userId = 1L;

        User user = mock(User.class);
        Orders order = mock(Orders.class);
        OrderProduct orderProduct = mock(OrderProduct.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderProductRepository.findAllByOrder_User_Id(userId))
                .thenReturn(List.of(orderProduct));

        when(orderProduct.getOrder()).thenReturn(order);
        when(order.getOrderNumber()).thenReturn(12312312L);
        when(order.getId()).thenReturn(100L);
        when(order.getTotalPrice()).thenReturn(10000.0);
        when(order.getUsedPoint()).thenReturn(2000.0);
        when(order.getStatus()).thenReturn(null);
        when(orderProduct.getCurrency()).thenReturn("KRW");
        when(orderProduct.getCreatedAt()).thenReturn(LocalDateTime.now());

        // when
        List<getAllOrderProductResponse> result =
                orderProductService.getAllOrderProducts(userId);

        // then
        assertThat(result).hasSize(1);
        getAllOrderProductResponse response = result.get(0);

        assertThat(response.getOrderNumber()).isEqualTo(12312312L);
        assertThat(response.getFinalAmount()).isEqualTo(8000);

        verify(userRepository).findById(userId);
        verify(orderProductRepository).findAllByOrder_User_Id(userId);
    }

    @Test
    void getOneOrderProducts_성공() {
        // given
        Long userId = 1L;
        Long orderId = 10L;

        User user = mock(User.class);
        Orders order = mock(Orders.class);
        OrderProduct orderProduct = mock(OrderProduct.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderProductRepository.findAllByOrder_Id(orderId))
                .thenReturn(List.of(orderProduct));

        when(order.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        when(order.getOrderNumber()).thenReturn(12312312L);
        when(order.getId()).thenReturn(orderId);
        when(order.getTotalPrice()).thenReturn(20000.0);
        when(order.getUsedPoint()).thenReturn(5000.0);
        when(order.getStatus()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(orderProduct.getCurrency()).thenReturn("KRW");

        // when
        getOneOrderProductResponse response =
                orderProductService.getOneOrderProducts(userId, orderId);

        // then
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getFinalAmount()).isEqualTo(15000);

        verify(orderRepository).findById(orderId);
        verify(orderProductRepository).findAllByOrder_Id(orderId);
    }

    @Test
    void getOneOrderProducts_본인주문아닐경우_예외() {
        // given
        Long userId = 1L;
        Long orderId = 10L;

        User loginUser = mock(User.class);
        User otherUser = mock(User.class);
        Orders order = mock(Orders.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(loginUser));

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(order.getUser()).thenReturn(otherUser);
        when(otherUser.getId()).thenReturn(999L);

        // when & then
        assertThatThrownBy(() ->
                orderProductService.getOneOrderProducts(userId, orderId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 주문만 조회할 수 있습니다.");
    }


}
