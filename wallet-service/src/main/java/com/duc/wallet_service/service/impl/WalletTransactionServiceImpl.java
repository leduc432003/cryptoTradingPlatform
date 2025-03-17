package com.duc.wallet_service.service.impl;

import com.duc.wallet_service.dto.UserDTO;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;
import com.duc.wallet_service.repository.WalletRepository;
import com.duc.wallet_service.repository.WalletTransactionRepository;
import com.duc.wallet_service.service.UserService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public List<WalletTransaction> getTransactionsByFilters(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        return walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);
    }

    @Override
    public double getTotalAmountByFilters(Long days, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        double totalAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() != WalletTransactionType.REFUND_BUY_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        double refundAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.REFUND_BUY_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        return totalAmount - refundAmount;
    }

    @Override
    public double getTotalAmountByDateRange(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) throws Exception {
        if (startDate == null || endDate == null) {
            throw new Exception("startDate và endDate không được để trống.");
        }

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        double totalAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() != WalletTransactionType.REFUND_BUY_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        double refundAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.REFUND_BUY_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        return totalAmount - refundAmount;
    }

    @Override
    public List<List<Object>> getTotalAmountByDateWithTimestamp(Long days, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        return transactions.stream()
                .collect(Collectors.groupingBy(WalletTransaction::getDate))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<WalletTransaction> dailyTransactions = entry.getValue();

                    double totalAmount = dailyTransactions.stream()
                            .filter(tx -> tx.getWalletTransactionType() != WalletTransactionType.REFUND_BUY_ASSET)
                            .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                            .sum();

                    double refundAmount = dailyTransactions.stream()
                            .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.REFUND_BUY_ASSET)
                            .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                            .sum();

                    double netAmount = totalAmount - refundAmount;

                    long timestamp = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
                    return Arrays.<Object>asList(timestamp, netAmount);
                })
                .sorted(Comparator.comparing(o -> (Long) o.get(0))) // Sort by timestamp
                .collect(Collectors.toList());
    }

    @Override
    public List<List<Object>> getTotalAmountByMonthWithTimestamp(Long months, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (months == null) ? LocalDate.MIN : endDate.minusMonths(months);

        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        // Nhóm các giao dịch theo tháng
        Map<YearMonth, List<WalletTransaction>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(tx -> YearMonth.from(tx.getDate())));

        // Xử lý từng nhóm (tháng)
        return transactionsByMonth.entrySet().stream()
                .map(entry -> {
                    YearMonth yearMonth = entry.getKey(); // Lấy tháng
                    List<WalletTransaction> monthlyTransactions = entry.getValue(); // Lấy danh sách giao dịch trong tháng

                    // Tính tổng số tiền trong tháng
                    double totalAmount = monthlyTransactions.stream()
                            .filter(tx -> tx.getWalletTransactionType() != WalletTransactionType.REFUND_BUY_ASSET)
                            .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                            .sum();

                    // Tính tổng số tiền của REFUND_BUY_ASSET trong tháng
                    double refundAmount = monthlyTransactions.stream()
                            .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.REFUND_BUY_ASSET)
                            .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                            .sum();

                    double netAmount = totalAmount - refundAmount;

                    // Tạo timestamp cho ngày đầu tiên của tháng
                    long timestamp = yearMonth.atDay(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;

                    // Trả về danh sách chứa timestamp và tổng số tiền sau khi trừ REFUND
                    return Arrays.<Object>asList(timestamp, netAmount);
                })
                .sorted(Comparator.comparing(o -> (Long) o.get(0))) // Sắp xếp theo timestamp
                .collect(Collectors.toList());
    }

    @Override
    public long countBuyAndSellAssetTransactions(Long walletId) {
        return walletTransactionRepository.countByWallet_IdAndWalletTransactionTypeIn(
                walletId, new WalletTransactionType[]{WalletTransactionType.BUY_ASSET, WalletTransactionType.SELL_ASSET});
    }
}
