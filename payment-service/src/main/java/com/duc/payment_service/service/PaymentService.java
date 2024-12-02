package com.duc.payment_service.service;

import com.duc.payment_service.model.Payment;

import java.math.BigDecimal;

public interface PaymentService {
    Payment createPaymentRequest(Long userId, BigDecimal amount) throws Exception;
    boolean checkPaymentStatus(Long paymentId) throws Exception;
}
