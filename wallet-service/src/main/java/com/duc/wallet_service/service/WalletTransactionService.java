package com.duc.wallet_service.service;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface WalletTransactionService {
    WalletTransaction createWalletTransaction(Wallet wallet, WalletTransactionType transactionType, String transferId, String purpose, BigDecimal amount);
    WalletTransaction findByWallet(Long walletId);
    List<WalletTransaction> getWalletTransactionService(Long walletId);
    List<WalletTransaction> getAllWalletTransaction();
}
