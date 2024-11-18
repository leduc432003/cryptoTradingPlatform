package com.duc.trading_service.service;

import com.duc.trading_service.dto.WalletDTO;
import com.duc.trading_service.dto.request.AddBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service", url = "http://localhost:5004")
public interface WalletService {
    @PutMapping("/api/wallet/order/{orderId}/pay")
    WalletDTO payOrderPayment(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId);
    @PostMapping("/api/wallet")
    WalletDTO addBalance(@RequestHeader("Internal-Service-Token") String internalJwt, @RequestBody AddBalanceRequest request);
    @GetMapping("/api/wallet")
    WalletDTO getUserWallet(@RequestHeader("Authorization") String jwt);
}
