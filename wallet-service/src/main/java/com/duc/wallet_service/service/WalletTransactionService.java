package com.duc.wallet_service.service;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface WalletTransactionService {
    WalletTransaction createWalletTransaction(Wallet wallet, WalletTransactionType transactionType, String transferId, String purpose, BigDecimal amount);
    WalletTransaction findByWallet(Long walletId);
    List<WalletTransaction> getWalletTransactionService(Long walletId);
    List<WalletTransaction> getWalletTransactionsByWalletIdAndDaysAndType(Long walletId, Long days, WalletTransactionType transactionType);
    List<WalletTransaction> getTransactionsByFilters(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes);
    double getTotalAmountByFilters(Long days, List<WalletTransactionType> transactionTypes);

    double getTotalFeeAmountByFilters(Long days, List<WalletTransactionType> transactionTypes);

    double getTotalAmountByDateRange(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) throws Exception;

    double getTotalFeeAmountByDateRange(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) throws Exception;

    List<List<Object>> getTotalAmountByDateWithTimestamp(String startDateStr, String endDateStr, Long days, List<WalletTransactionType> transactionTypes);

    List<List<Object>> getTotalFeeAmountByDateWithTimestamp(String startDateStr, String endDateStr, Long days, List<WalletTransactionType> transactionTypes);

    List<List<Object>> getTotalAmountByMonthWithTimestamp(Long months, List<WalletTransactionType> transactionTypes);

    List<List<Object>> getTotalFeeAmountByMonthWithTimestamp(Long months, List<WalletTransactionType> transactionTypes);

    long countBuyAndSellAssetTransactions(Long walletId);
}
