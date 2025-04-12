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
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
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
    public List<WalletTransaction> getWalletTransactionsByWalletIdAndDaysAndType(Long walletId, Long days, WalletTransactionType transactionType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        if (transactionType == null) {
            return walletTransactionRepository.findAllByWalletIdAndDateBetween(walletId, startDate, endDate);
        }

        return walletTransactionRepository.findAllByWalletIdAndDateBetweenAndType(
                walletId, startDate, endDate, transactionType);
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
    public List<List<Object>> getTotalAmountByDateWithTimestamp(String startDateStr, String endDateStr, Long days, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate;
        LocalDate startDate;

        // Ưu tiên days nếu được cung cấp
        if (days != null) {
            if (days < 0) {
                throw new IllegalArgumentException("Days cannot be negative.");
            }
            endDate = LocalDate.now();
            startDate = endDate.minusDays(days);
        }
        // Nếu không có days, kiểm tra startDate và endDate
        else if (startDateStr != null && endDateStr != null) {
            try {
                startDate = LocalDate.parse(startDateStr);
                endDate = LocalDate.parse(endDateStr);
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("startDate cannot be after endDate.");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
            }
        }
        // Nếu không có days, startDate, endDate, lấy toàn bộ dữ liệu
        else {
            endDate = LocalDate.now();
            startDate = LocalDate.MIN; // Lấy toàn bộ lịch sử giao dịch
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        // Nhóm giao dịch theo ngày
        Map<LocalDate, List<WalletTransaction>> transactionsByDate = transactions.stream()
                .collect(Collectors.groupingBy(WalletTransaction::getDate));

        // Tạo danh sách kết quả cho tất cả các ngày trong khoảng thời gian
        List<List<Object>> result = new ArrayList<>();
        LocalDate currentDate = startDate.equals(LocalDate.MIN)
                ? transactionsByDate.keySet().stream().min(LocalDate::compareTo).orElse(endDate)
                : startDate;

        while (!currentDate.isAfter(endDate)) {
            List<WalletTransaction> dailyTransactions = transactionsByDate.getOrDefault(currentDate, Collections.emptyList());

            double totalAmount = dailyTransactions.stream()
                    .filter(tx -> tx.getWalletTransactionType() != WalletTransactionType.REFUND_BUY_ASSET)
                    .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                    .sum();

            double refundAmount = dailyTransactions.stream()
                    .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.REFUND_BUY_ASSET)
                    .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                    .sum();

            double netAmount = totalAmount - refundAmount;

            long timestamp = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
            result.add(Arrays.<Object>asList(timestamp, netAmount));

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    @Override
    public List<List<Object>> getTotalAmountByMonthWithTimestamp(Long months, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        // Kiểm tra months
        if (months != null) {
            if (months < 0) {
                throw new IllegalArgumentException("Months cannot be negative.");
            }
            startDate = endDate.minusMonths(months);
        } else {
            startDate = LocalDate.MIN; // Lấy toàn bộ nếu months là null
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, transactionTypes);

        // Nhóm giao dịch theo tháng
        Map<YearMonth, List<WalletTransaction>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(tx -> YearMonth.from(tx.getDate())));

        // Tạo danh sách kết quả cho tất cả các tháng trong khoảng thời gian
        List<List<Object>> result = new ArrayList<>();
        YearMonth endYearMonth = YearMonth.from(endDate);
        YearMonth startYearMonth = startDate.equals(LocalDate.MIN)
                ? transactionsByMonth.keySet().stream().min(YearMonth::compareTo).orElse(endYearMonth)
                : YearMonth.from(startDate);

        YearMonth currentYearMonth = startYearMonth;
        while (!currentYearMonth.isAfter(endYearMonth)) {
            List<WalletTransaction> monthlyTransactions = transactionsByMonth.getOrDefault(currentYearMonth, Collections.emptyList());

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
            long timestamp = currentYearMonth.atDay(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;

            // Thêm vào kết quả
            result.add(Arrays.<Object>asList(timestamp, netAmount));

            // Chuyển sang tháng tiếp theo
            currentYearMonth = currentYearMonth.plusMonths(1);
        }

        return result;
    }

    @Override
    public long countBuyAndSellAssetTransactions(Long walletId) {
        return walletTransactionRepository.countByWallet_IdAndWalletTransactionTypeIn(
                walletId, new WalletTransactionType[]{WalletTransactionType.BUY_ASSET, WalletTransactionType.SELL_ASSET});
    }
}
