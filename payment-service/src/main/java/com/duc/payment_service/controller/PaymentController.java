package com.duc.payment_service.controller;

import com.duc.payment_service.dto.UserDTO;
import com.duc.payment_service.model.Payment;
import com.duc.payment_service.model.PaymentStatus;
import com.duc.payment_service.service.PaymentService;
import com.duc.payment_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Payment> createPaymentRequest(
            @RequestHeader("Authorization") String jwt, @RequestParam BigDecimal amount) {
        UserDTO user = userService.getUserProfile(jwt);
        Payment payment = paymentService.createPaymentRequest(user.getId(), amount);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/check/{paymentId}")
    public ResponseEntity<Map<String, String>> checkPaymentStatus(@RequestHeader("Authorization") String jwt, @PathVariable Long paymentId) {
        try {
            boolean isSuccessful = paymentService.checkPaymentStatus(paymentId);
            PaymentStatus status = isSuccessful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            return ResponseEntity.ok(Map.of("status", status.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}