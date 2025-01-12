package com.duc.payment_service.service;

import com.duc.payment_service.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Payment createPaymentRequest(Long userId, BigDecimal amount) throws Exception;
    boolean checkPaymentStatus(Long paymentId) throws Exception;
    List<Payment> getPayments(Long userId) throws Exception;
}
