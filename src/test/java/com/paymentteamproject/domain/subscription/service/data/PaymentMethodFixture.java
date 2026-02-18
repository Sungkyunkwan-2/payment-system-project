package com.paymentteamproject.domain.subscription.service.data;

import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.consts.PgProvider;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.user.entity.User;

public class PaymentMethodFixture {

    public static final User DEFAULT_USER = UserFixture.createUser() ;
    public static final String DEFAULT_BILLING_KEY = "billing-key-123";
    public static final String DEFAULT_CUSTOMER_UID = "customer-uid-123";
    public static final PaymentMethodStatus DEFAULT_PAYMENT_METHOD_STATUS = PaymentMethodStatus.ACTIVE;

    public static PaymentMethod createPaymentMethod()
    {
        return new PaymentMethod(DEFAULT_USER, DEFAULT_BILLING_KEY, DEFAULT_CUSTOMER_UID, DEFAULT_PAYMENT_METHOD_STATUS);
    }
}
