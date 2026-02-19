package com.paymentteamproject.domain.order.entity;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrdersTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .username("테스트유저")
                .build();
    }

    @Nested //비슷한 기능 묶기
    class 주문_생성_테스트 {
        @Test
        void 정상적인_주문생성_성공() {
            // given, when
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .usedPoint(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // then
            assertThat(order.getUser()).isEqualTo(testUser);
            assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }
    }

    @Nested
    class MarkRefunded_메서드_테스트 {
        @Test
        void 주문_완료_상태에서_환불_성공() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.ORDER_COMPLETED)
                    .build();

            // when
            order.markRefunded();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER_CANCELED);
        }

        @Test
        void 주문_완료_상태아닐시_환불_실패() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.ORDER_CANCELED)
                    .build();

            // when, then
            assertThatThrownBy(order::markRefunded)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("주문 완료 상태만 환불할 수 있습니다.");
        }
    }

    @Nested
    class isRefunded_메서드_테스트{
        @Test
        void isRefunded_True() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.ORDER_CANCELED)
                    .build();

            // when, then
            assertThat(order.isRefunded()).isTrue();
        }

        @Test
        void isRefunded_False() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.ORDER_COMPLETED)
                    .build();

            // when, then
            assertThat(order.isRefunded()).isFalse();
        }
    }

    @Nested
    class SetUsedPointTest_메서드_테스트 {
        @Test
        void 정상적인_포인트_설정() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when
            order.setUsedPoint(BigDecimal.valueOf(5000));

            // then
            assertThat(order.getUsedPoint()).isEqualTo(BigDecimal.valueOf(5000));
        }

        @Test
        void Null_포인트는_0으로() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when
            order.setUsedPoint(null);

            // then
            assertThat(order.getUsedPoint()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void 음수_포인트_사용_실패() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when, then
            assertThatThrownBy(() -> order.setUsedPoint(BigDecimal.valueOf(-1000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용 포인트는 음수일 수 없습니다.");
        }

        @Test
        void 주문금액보다_초과_포인트_사용_실패() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when, then
            assertThatThrownBy(() -> order.setUsedPoint(BigDecimal.valueOf(15000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용 포인트가 주문 금액을 초과할 수 없습니다.");
        }
    }

    @Nested
    class GetActualPaymentAmount_메서드_테스트 {
        @Test
        void 포인트_미사용시_전체_금액_반환() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .usedPoint(null)
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when
            BigDecimal totalAmount = order.getActualPaymentAmount();

            // then
            assertThat(totalAmount).isEqualTo(BigDecimal.valueOf(10000));
        }

        @Test
        void 포인트_사용시_차감된_금액_반환() {
            // given
            Orders order = Orders.builder()
                    .user(testUser)
                    .totalPrice(BigDecimal.valueOf(10000))
                    .usedPoint(BigDecimal.valueOf(3000))
                    .status(OrderStatus.PAYMENT_PENDING)
                    .build();

            // when
            BigDecimal actualAmount = order.getActualPaymentAmount();

            // then
            assertThat(actualAmount).isEqualTo(BigDecimal.valueOf(7000));
        }

    }
}