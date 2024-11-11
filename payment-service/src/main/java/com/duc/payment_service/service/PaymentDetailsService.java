package com.duc.payment_service.service;

import com.duc.payment_service.model.PaymentDetails;

public interface PaymentDetailsService {
    PaymentDetails createPayment(PaymentDetails payment);
    PaymentDetails getUserPayment(Long userId);
}
