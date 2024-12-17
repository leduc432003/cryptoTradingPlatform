package com.duc.withdrawal_service.service;

import com.duc.withdrawal_service.dto.PaymentDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service", url = "http://localhost:5009")
public interface PaymentDetailsService {
    @GetMapping("/api/payment-details/admin/{id}")
    PaymentDetailsDTO getUserPaymentDetailsById(@RequestHeader("Authorization") String jwt, @PathVariable Long id);
    @GetMapping("/api/payment-details")
    PaymentDetailsDTO getUserPaymentDetails(@RequestHeader("Authorization") String jwt);
}
