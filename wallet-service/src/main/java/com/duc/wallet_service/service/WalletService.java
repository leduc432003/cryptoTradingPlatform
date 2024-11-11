package com.duc.wallet_service.service;

import com.duc.wallet_service.model.Wallet;

import java.math.BigDecimal;

public interface WalletService {
    Wallet getWalletByUserId(Long userId);
    Wallet addBalance(Wallet wallet, Long money);
    Wallet findWalletById(Long walletId) throws Exception;
    Wallet transferToAnotherWallet(Long senderId, Wallet receiverWallet, BigDecimal amount) throws Exception;
    Wallet payOrderPayment(Long orderId, Long userId, String jwt) throws Exception;
}
