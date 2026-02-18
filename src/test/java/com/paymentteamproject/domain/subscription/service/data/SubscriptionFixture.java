package com.paymentteamproject.domain.subscription.service.data;

import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.user.entity.User;

public class SubscriptionFixture {

    public static final User DEFAULT_USER = UserFixture.createUser() ;
    public static final Plan DEFAULT_NAME = PlanFixture.createPlan() ;
    public static final PaymentMethod DEFAULT_PAYMENT_METHOD =  PaymentMethodFixture.createPaymentMethod() ;
    public static final SubscriptionStatus DEFAULT_SUBSCRIPTION_STATUS = SubscriptionStatus.ACTIVE;

    public static Subscription createSubscription() {
        return new Subscription(DEFAULT_USER, DEFAULT_NAME, DEFAULT_PAYMENT_METHOD, DEFAULT_SUBSCRIPTION_STATUS);
    }
}
