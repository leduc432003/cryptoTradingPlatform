package com.duc.payment_service.service.impl;

import com.duc.payment_service.model.PaymentDetails;
import com.duc.payment_service.repository.PaymentDetailsRepository;
import com.duc.payment_service.service.PaymentDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentDetailsServiceImpl implements PaymentDetailsService {
    private final PaymentDetailsRepository paymentRepository;

    @Override
    public PaymentDetails createPayment(PaymentDetails payment) {
        PaymentDetails createPayment = PaymentDetails.builder()
                .accountName(payment.getAccountName())
                .accountNumber(payment.getAccountNumber())
                .bankName(payment.getBankName())
                .userId(payment.getUserId())
                .build();
        return paymentRepository.save(createPayment);
    }

    @Override
    public PaymentDetails getUserPayment(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
