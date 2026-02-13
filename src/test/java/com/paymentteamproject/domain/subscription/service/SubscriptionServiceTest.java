package com.paymentteamproject.domain.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.paymentMethod.repository.PaymentMethodRepository;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.plan.exception.PlanNotFoundException;
import com.paymentteamproject.domain.plan.repository.PlanRepository;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import com.paymentteamproject.domain.subscription.service.data.PlanFixture;
import com.paymentteamproject.domain.subscription.service.data.UserFixture;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private String email;
    private String planId;
    private String billingKey;
    private String customerUid;


    @BeforeEach
    void setUp() {
        email = "test@test.com";
        planId = "plan-123";
        billingKey = "billing-key-123";
        customerUid = "customer-uid-123";
    }


    @Test
    @DisplayName("구독 생성 성공")
    void createSubscription_success() {
        // given

        CreateSubscriptionRequest request = createSubscriptionRequest(planId, billingKey, customerUid);
        User user = UserFixture.createUser();
        Plan plan = PlanFixture.createPlan();

        PaymentMethod savedPaymentMethod = new PaymentMethod(
                user,
                billingKey,
                customerUid,
                PaymentMethodStatus.ACTIVE
        );
        ReflectionTestUtils.setField(savedPaymentMethod, "id", 1L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(planRepository.findByPlanId(planId))
                .thenReturn(Optional.of(plan));
        when(paymentMethodRepository.save(any(PaymentMethod.class)))
                .thenReturn(savedPaymentMethod);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> {
                    Subscription s = invocation.getArgument(0);
                    ReflectionTestUtils.setField(s, "subscriptionId", "SUB-123");
                    return s;
                });

        // when
        CreateSubscriptionResponse response = subscriptionService.create(email, request);

        // then
        assertNotNull(response);
        assertEquals("SUB-123", response.getSubscriptionId());

        verify(userRepository).findByEmail(email);
        verify(planRepository).findByPlanId(planId);
        verify(paymentMethodRepository).save(any(PaymentMethod.class));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("구독 생성 시 PaymentMethod가 올바르게 생성됨")
    void createSubscription_paymentMethodCreatedCorrectly() {
        // given
        CreateSubscriptionRequest request = createSubscriptionRequest(planId, billingKey, customerUid);
        User user = UserFixture.createUser();
        Plan plan = PlanFixture.createPlan();

        ArgumentCaptor<PaymentMethod> paymentMethodCaptor = ArgumentCaptor.forClass(PaymentMethod.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(planRepository.findByPlanId(planId)).thenReturn(Optional.of(plan));
        when(paymentMethodRepository.save(any(PaymentMethod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> {
                    Subscription s = invocation.getArgument(0);
                    ReflectionTestUtils.setField(s, "subscriptionId", "SUB-123");
                    return s;
                });

        // when
        subscriptionService.create(email, request);

        // then
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture());
        PaymentMethod capturedPaymentMethod = paymentMethodCaptor.getValue();

        assertEquals(billingKey, capturedPaymentMethod.getBillingKey());
        assertEquals(customerUid, capturedPaymentMethod.getCustomerUid());
        assertEquals(PaymentMethodStatus.ACTIVE, capturedPaymentMethod.getStatus());
        assertEquals(user, capturedPaymentMethod.getUser());
    }

    @Test
    @DisplayName("구독 생성 시 Subscription이 올바르게 생성됨")
    void createSubscription_subscriptionCreatedCorrectly() {
        // given
        CreateSubscriptionRequest request = createSubscriptionRequest(planId, billingKey, customerUid);
        User user = UserFixture.createUser();
        Plan plan = PlanFixture.createPlan();

        PaymentMethod savedPaymentMethod = new PaymentMethod(
                user, billingKey, customerUid, PaymentMethodStatus.ACTIVE
        );
        ReflectionTestUtils.setField(savedPaymentMethod, "id", 1L);

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(planRepository.findByPlanId(planId)).thenReturn(Optional.of(plan));
        when(paymentMethodRepository.save(any(PaymentMethod.class)))
                .thenReturn(savedPaymentMethod);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> {
                    Subscription s = invocation.getArgument(0);
                    ReflectionTestUtils.setField(s, "subscriptionId", "SUB-123");
                    return s;
                });

        // when
        subscriptionService.create(email, request);

        // then
        verify(subscriptionRepository).save(subscriptionCaptor.capture());
        Subscription capturedSubscription = subscriptionCaptor.getValue();

        assertEquals(user, capturedSubscription.getUser());
        assertEquals(plan, capturedSubscription.getPlan());
        assertSame(savedPaymentMethod, capturedSubscription.getPaymentMethod());
        assertNotNull(capturedSubscription.getCurrentPeriodEnd());
    }

    @Test
    @DisplayName("구독 생성 실패 - 사용자를 찾을 수 없음")
    void createSubscription_userNotFound() {
        // given
        CreateSubscriptionRequest request = createSubscriptionRequest(planId, billingKey, customerUid);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> subscriptionService.create(email, request)
        );

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());

        verify(userRepository).findByEmail(email);
        verify(planRepository, never()).findByPlanId(anyString());
        verify(paymentMethodRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("구독 생성 실패 - 플랜을 찾을 수 없음")
    void createSubscription_planNotFound() {
        // given
        CreateSubscriptionRequest request = createSubscriptionRequest(planId, billingKey, customerUid);
        User user = UserFixture.createUser();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(planRepository.findByPlanId(planId))
                .thenReturn(Optional.empty());

        // when & then
        PlanNotFoundException exception = assertThrows(
                PlanNotFoundException.class,
                () -> subscriptionService.create(email, request)
        );

        assertEquals("존재하지 않는 구독 플랜입니다.", exception.getMessage());

        verify(userRepository).findByEmail(email);
        verify(planRepository).findByPlanId(planId);
        verify(paymentMethodRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("구독 생성 실패 - email 이 null")
    void createSubscription_emailIsNull() {
        // given
        CreateSubscriptionRequest request =
                createSubscriptionRequest(planId, billingKey, customerUid);

        // when & then
        assertThrows(
                UserNotFoundException.class,
                () -> subscriptionService.create(null, request)
        );
    }


    // Helper method
    private CreateSubscriptionRequest createSubscriptionRequest(String planId, String billingKey, String customerUid) {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        ReflectionTestUtils.setField(request, "planId", planId);
        ReflectionTestUtils.setField(request, "billingKey", billingKey);
        ReflectionTestUtils.setField(request, "customerUid", customerUid);
        return request;
    }
}