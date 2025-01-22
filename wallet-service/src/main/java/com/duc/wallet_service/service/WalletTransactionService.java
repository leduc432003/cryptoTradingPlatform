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
    List<WalletTransaction> getTransactionsByFilters(Long days, WalletTransactionType transactionType);
    double getTotalAmountByFilters(Long days, WalletTransactionType transactionType);
    List<List<Object>> getTotalAmountByDateWithTimestamp(Long days, WalletTransactionType transactionType);
    long countBuyAndSellAssetTransactions(Long walletId);
}
