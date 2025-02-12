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
    public PaymentDetails createPayment(PaymentDetails payment) throws Exception {
        PaymentDetails paymentAccountExist = getUserPayment(payment.getUserId());
        if(paymentAccountExist != null) {
            throw new Exception("Payment Account is exist.");
        }
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

    @Override
    public void deletePaymentAccount(Long userId) throws Exception {
        PaymentDetails paymentAccountExist = getUserPayment(userId);
        if(paymentAccountExist != null) {
            paymentRepository.deleteById(paymentAccountExist.getId());
        } else {
            throw new Exception("No payment account found");
        }
    }
}
