package com.duc.wallet_service.controller;

import com.duc.wallet_service.dto.UserDTO;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.service.UserService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WalletTransactionController {
    private final UserService userService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;

    @GetMapping
    public ResponseEntity<List<WalletTransaction>> getUserWallet(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        List<WalletTransaction> walletTransactions = walletTransactionService.getWalletTransactionService(wallet.getId());
        return new ResponseEntity<>(walletTransactions, HttpStatus.OK);
    }
}
