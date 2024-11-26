package com.duc.wallet_service.controller;

import com.duc.wallet_service.dto.UserDTO;
import com.duc.wallet_service.dto.UserRole;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.service.UserService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WalletTransactionController {
    private final UserService userService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;

    @GetMapping
    public ResponseEntity<List<WalletTransaction>> getUserWalletTransaction(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        List<WalletTransaction> walletTransactions = walletTransactionService.getWalletTransactionService(wallet.getId());
        return new ResponseEntity<>(walletTransactions, HttpStatus.OK);
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<List<WalletTransaction>> getUserWalletTransactionAdmin(@RequestHeader("Authorization") String jwt, @PathVariable Long userId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        Wallet wallet = walletService.getWalletByUserId(userId);
        return new ResponseEntity<>(walletTransactionService.getWalletTransactionService(wallet.getId()), HttpStatus.OK);
    }

    @GetMapping("/admin/transaction")
    public ResponseEntity<List<WalletTransaction>> getAllTransaction(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        return new ResponseEntity<>(walletTransactionService.getAllWalletTransaction(), HttpStatus.OK);
    }
}
