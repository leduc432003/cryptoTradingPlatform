package com.duc.wallet_service.service.impl;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;
import com.duc.wallet_service.repository.WalletTransactionRepository;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public WalletTransaction createWalletTransaction(Wallet wallet, WalletTransactionType transactionType, String transferId, String purpose, BigDecimal amount) {
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletTransactionType(transactionType)
                .wallet(wallet)
                .purpose(purpose)
                .transferId(transferId)
                .date(LocalDate.now())
                .amount(amount)
                .build();

        return walletTransactionRepository.save(walletTransaction);
    }

    @Override
    public WalletTransaction findByWallet(Long walletId) {
        return walletTransactionRepository.findByWalletId(walletId);
    }

    @Override
    public List<WalletTransaction> getWalletTransactionService(Long walletId) {
        return walletTransactionRepository.findAllByWalletId(walletId);
    }

    @Override
    public List<WalletTransaction> getTransactionsByFilters(Long days, WalletTransactionType transactionType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetween(startDate, endDate);
        return transactions.stream()
                .filter(tx -> transactionType == null || tx.getWalletTransactionType() == transactionType)
                .toList();
    }

    @Override
    public double getTotalAmountByFilters(Long days, WalletTransactionType transactionType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetween(startDate, endDate);

        return transactions.stream()
                .filter(tx -> transactionType == null || tx.getWalletTransactionType() == transactionType)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();
    }

    @Override
    public List<List<Object>> getTotalAmountByDateWithTimestamp(Long days, WalletTransactionType transactionType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetween(startDate, endDate);

        return transactions.stream()
                .filter(tx -> transactionType == null || tx.getWalletTransactionType() == transactionType)
                .collect(Collectors.groupingBy(tx -> tx.getDate()))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    double totalAmount = entry.getValue().stream()
                            .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                            .sum();

                    long timestamp = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;

                    List<Object> result = List.of(timestamp, totalAmount);
                    return result;
                })
                .collect(Collectors.toList());
    }
}
