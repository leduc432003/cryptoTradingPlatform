package com.duc.wallet_service.controller;

import com.duc.wallet_service.dto.UserDTO;
import com.duc.wallet_service.dto.request.AddBalanceRequest;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;
import com.duc.wallet_service.service.UserService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final UserService userService;
    @Value("${internal.service.token}")
    private String internalServiceToken;
    private final WalletTransactionService walletTransactionService;

    @PostMapping
    public ResponseEntity<Wallet> addBalance(@RequestHeader("Internal-Service-Token") String internalJwt, @RequestBody AddBalanceRequest request) {
        if (!internalServiceToken.equals(internalJwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Wallet wallet = walletService.getWalletByUserId(request.getUserId());
        walletService.addBalance(wallet, request.getMoney());
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Wallet> getUserWallet(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @PutMapping("/{walletId}/transfer")
    public ResponseEntity<Wallet> transferToAnotherWallet(@RequestHeader("Authorization") String jwt, @PathVariable Long walletId, @RequestBody WalletTransaction walletTransaction) throws Exception {
        UserDTO senderUser = userService.getUserProfile(jwt);
        Wallet receiverWallet = walletService.findWalletById(walletId);
        Wallet wallet = walletService.transferToAnotherWallet(senderUser.getId(), receiverWallet, walletTransaction.getAmount());
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @PutMapping("/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Wallet wallet = walletService.payOrderPayment(orderId, user.getId(), jwt);
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }
}
