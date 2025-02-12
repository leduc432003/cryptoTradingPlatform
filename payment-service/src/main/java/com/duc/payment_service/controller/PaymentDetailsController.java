package com.duc.payment_service.controller;

import com.duc.payment_service.dto.UserDTO;
import com.duc.payment_service.dto.UserRole;
import com.duc.payment_service.model.PaymentDetails;
import com.duc.payment_service.service.PaymentDetailsService;
import com.duc.payment_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment-details")
public class PaymentDetailsController {
    private final UserService userService;
    private final PaymentDetailsService paymentDetailsService;

    @PostMapping
    public ResponseEntity<PaymentDetails> createPaymentDetails(@RequestHeader("Authorization") String jwt, @RequestBody PaymentDetails request) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        request.setUserId(user.getId());
        PaymentDetails paymentDetails = paymentDetailsService.createPayment(request);
        return new ResponseEntity<>(paymentDetails, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaymentDetails> getUserPaymentDetails(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        PaymentDetails paymentDetails = paymentDetailsService.getUserPayment(user.getId());
        return new ResponseEntity<>(paymentDetails, HttpStatus.CREATED);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<PaymentDetails> getUserPaymentDetailsById(@RequestHeader("Authorization") String jwt, @PathVariable Long id) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(!user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new Exception("Only admin can payment details");
        }

        PaymentDetails paymentDetails = paymentDetailsService.getUserPayment(id);
        return new ResponseEntity<>(paymentDetails, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePaymentAccount(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        paymentDetailsService.deletePaymentAccount(user.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
