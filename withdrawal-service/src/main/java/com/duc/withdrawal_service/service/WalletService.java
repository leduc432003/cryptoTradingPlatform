package com.duc.withdrawal_service.service;

import com.duc.withdrawal_service.dto.WalletDTO;
import com.duc.withdrawal_service.dto.request.AddBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service", url = "http://localhost:5004")
public interface WalletService {
    @GetMapping("/api/wallet")
    WalletDTO getUserWallet(@RequestHeader("Authorization") String jwt);
    @PostMapping("/api/wallet")
    WalletDTO addBalance(@RequestHeader("Internal-Service-Token") String internalJwt, @RequestBody AddBalanceRequest request);
}
