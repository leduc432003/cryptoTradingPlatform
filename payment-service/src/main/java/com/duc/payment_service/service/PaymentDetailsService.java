package com.duc.payment_service.service;

import com.duc.payment_service.model.PaymentDetails;

public interface PaymentDetailsService {
    PaymentDetails createPayment(PaymentDetails payment) throws Exception;
    PaymentDetails getUserPayment(Long userId);
    void deletePaymentAccount(Long userId);
}
