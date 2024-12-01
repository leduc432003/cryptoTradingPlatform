package com.duc.payment_service.service;

import com.duc.payment_service.dto.WalletDTO;
import com.duc.payment_service.dto.request.AddBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service", url = "http://localhost:5004")
public interface WalletService {
    @PostMapping("/api/wallet")
    WalletDTO addBalance(@RequestHeader("Internal-Service-Token") String internalJwt, @RequestBody AddBalanceRequest request);
}
