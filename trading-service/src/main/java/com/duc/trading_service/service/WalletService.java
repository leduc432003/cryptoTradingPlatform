package com.duc.trading_service.service;

import com.duc.trading_service.dto.WalletDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "wallet-service", url = "http://localhost:5004")
public interface WalletService {
    @PutMapping("/api/wallet/order/{orderId}/pay")
    WalletDTO payOrderPayment(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId);
}
