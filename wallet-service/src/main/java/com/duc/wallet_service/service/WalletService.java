package com.duc.wallet_service.service;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransactionType;

import java.math.BigDecimal;

public interface WalletService {
    Wallet getWalletByUserId(Long userId);
    Wallet addBalance(Wallet wallet, double money, WalletTransactionType transactionType);
    Wallet findWalletById(Long walletId) throws Exception;
    Wallet transferToAnotherWallet(Long senderId, Wallet receiverWallet, BigDecimal amount) throws Exception;
    Wallet payOrderPayment(Long orderId, Long userId, String jwt) throws Exception;
    void holdBalance(BigDecimal amount, Long userId) throws Exception;
    void releaseHeldBalance(BigDecimal amount, Long userId) throws Exception;
    void commitHeldBalance(BigDecimal amount, Long userId, WalletTransactionType transactionType) throws Exception;
}
