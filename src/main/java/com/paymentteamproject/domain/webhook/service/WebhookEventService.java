package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.domain.order.entity.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.order.service.OrderService;
import com.paymentteamproject.domain.orderProduct.entity.OrderProduct;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.webhook.dto.GetPaymentResponse;
import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import com.paymentteamproject.domain.webhook.repository.WebhookEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;
    private final PortOneClient portOneClient;
    private final PaymentRepository paymentRepository;


    @Transactional
    public void processWebhook(String webhookId, @Valid WebHookRequest request) {

        //멱등성 체크
        if (webhookEventRepository.existsByWebhookId(webhookId)) {
            log.info("이미 처리 되었습니다. webhookId: {}", webhookId);
            return;
        }

        //이벤트 기록
        WebhookEvent webhookEvent = new WebhookEvent(
                webhookId,
                request.getData().getPaymentId(),
                request.getData().getStatus()
        );
        webhookEventRepository.save(webhookEvent);

        String paymentId = request.getData().getPaymentId();
        GetPaymentResponse portOnePayment = portOneClient.getPayment(paymentId);

        if (!portOnePayment.getStatus().equals(request.getData().getStatus())){
            webhookEvent.fail();
            throw new IllegalStateException("상태가 일치하지 않습니다.");
        }

        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));


        Orders order = payment.getOrder();

        double portOneAmount = portOnePayment.getAmount().getTotal();
        double orderAmount = order.getTotalPrice();
        double epsilon = 0.0001;
        if(!(portOneAmount == orderAmount)){
            throw  new IllegalStateException("결제 금액 불일치");
        }



        webhookEvent.completeProcess();
    }

}




