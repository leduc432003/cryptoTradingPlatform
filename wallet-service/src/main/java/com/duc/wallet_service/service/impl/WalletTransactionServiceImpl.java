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

        // Tạo danh sách mới để bao gồm các loại phí nếu cần
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Nếu BUY_ASSET hoặc SELL_ASSET có trong danh sách, thêm các loại phí tương ứng
        if (extendedTransactionTypes.contains(WalletTransactionType.BUY_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (extendedTransactionTypes.contains(WalletTransactionType.SELL_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

        double totalAmount = transactions.stream()
                .mapToDouble(tx -> {
                    WalletTransactionType type = tx.getWalletTransactionType();
                    double amount = Math.abs(tx.getAmount().doubleValue());
                    // Cộng số tiền cho BUY_ASSET và SELL_ASSET
                    if (type == WalletTransactionType.BUY_ASSET || type == WalletTransactionType.SELL_ASSET) {
                        return amount;
                    } else if (type == WalletTransactionType.CUSTOMER_BUY_ASSET || type == WalletTransactionType.CUSTOMER_SELL_ASSET) {
                        // Trừ phí cho CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
                        return -amount;
                    }
                    // Các loại giao dịch khác đóng góp số tiền như bình thường
                    return amount;
                })
                .sum();

        return totalAmount;
    }

    @Override
    public double getTotalFeeAmountByFilters(Long days, List<WalletTransactionType> transactionTypes) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = (days == null) ? LocalDate.MIN : endDate.minusDays(days);

        // Tạo danh sách mới để bao gồm các loại phí
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Luôn thêm CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

        // Chỉ tính tổng phí từ CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        double totalFeeAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_BUY_ASSET ||
                        tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_SELL_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        return totalFeeAmount;
    }

    @Override
    public double getTotalAmountByDateRange(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) throws Exception {
        if (startDate == null || endDate == null) {
            throw new Exception("startDate và endDate không được để trống.");
        }

        // Tạo danh sách mới để bao gồm các loại phí nếu cần
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Nếu BUY_ASSET hoặc SELL_ASSET có trong danh sách, thêm các loại phí tương ứng
        if (extendedTransactionTypes.contains(WalletTransactionType.BUY_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (extendedTransactionTypes.contains(WalletTransactionType.SELL_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

        double totalAmount = transactions.stream()
                .mapToDouble(tx -> {
                    WalletTransactionType type = tx.getWalletTransactionType();
                    double amount = Math.abs(tx.getAmount().doubleValue());
                    // Cộng số tiền cho BUY_ASSET và SELL_ASSET
                    if (type == WalletTransactionType.BUY_ASSET || type == WalletTransactionType.SELL_ASSET) {
                        return amount;
                    } else if (type == WalletTransactionType.CUSTOMER_BUY_ASSET || type == WalletTransactionType.CUSTOMER_SELL_ASSET) {
                        // Trừ phí cho CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
                        return -amount;
                    }
                    // Các loại giao dịch khác đóng góp số tiền như bình thường
                    return amount;
                })
                .sum();

        return totalAmount;
    }

    @Override
    public double getTotalFeeAmountByDateRange(LocalDate startDate, LocalDate endDate, List<WalletTransactionType> transactionTypes) throws Exception {
        if (startDate == null || endDate == null) {
            throw new Exception("startDate và endDate không được để trống.");
        }

        // Tạo danh sách mới để bao gồm các loại phí
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Luôn thêm CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

        // Chỉ tính tổng phí từ CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        double totalFeeAmount = transactions.stream()
                .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_BUY_ASSET ||
                        tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_SELL_ASSET)
                .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                .sum();

        return totalFeeAmount;
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

        // Tạo danh sách mới để bao gồm các loại phí nếu cần
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Nếu BUY_ASSET hoặc SELL_ASSET có trong danh sách, thêm các loại phí tương ứng
        if (extendedTransactionTypes.contains(WalletTransactionType.BUY_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (extendedTransactionTypes.contains(WalletTransactionType.SELL_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

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
                    .mapToDouble(tx -> {
                        WalletTransactionType type = tx.getWalletTransactionType();
                        double amount = Math.abs(tx.getAmount().doubleValue());
                        // Cộng số tiền cho BUY_ASSET và SELL_ASSET
                        if (type == WalletTransactionType.BUY_ASSET || type == WalletTransactionType.SELL_ASSET) {
                            return amount;
                        } else if (type == WalletTransactionType.CUSTOMER_BUY_ASSET || type == WalletTransactionType.CUSTOMER_SELL_ASSET) {
                            // Trừ phí cho CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
                            return -amount;
                        }
                        // Các loại giao dịch khác đóng góp số tiền như bình thường
                        return amount;
                    })
                    .sum();

            long timestamp = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
            result.add(Arrays.<Object>asList(timestamp, totalAmount));

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    @Override
    public List<List<Object>> getTotalFeeAmountByDateWithTimestamp(String startDateStr, String endDateStr, Long days, List<WalletTransactionType> transactionTypes) {
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

        // Tạo danh sách mới để bao gồm các loại phí
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Luôn thêm CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

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

            double totalFeeAmount = dailyTransactions.stream()
                    .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_BUY_ASSET ||
                            tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_SELL_ASSET)
                    .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                    .sum();

            long timestamp = currentDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
            result.add(Arrays.<Object>asList(timestamp, totalFeeAmount));

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

        // Tạo danh sách mới để bao gồm các loại phí nếu cần
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Nếu BUY_ASSET hoặc SELL_ASSET có trong danh sách, thêm các loại phí tương ứng
        if (extendedTransactionTypes.contains(WalletTransactionType.BUY_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (extendedTransactionTypes.contains(WalletTransactionType.SELL_ASSET) && !extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

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
                    .mapToDouble(tx -> {
                        WalletTransactionType type = tx.getWalletTransactionType();
                        double amount = Math.abs(tx.getAmount().doubleValue());
                        // Cộng số tiền cho BUY_ASSET và SELL_ASSET
                        if (type == WalletTransactionType.BUY_ASSET || type == WalletTransactionType.SELL_ASSET) {
                            return amount;
                        } else if (type == WalletTransactionType.CUSTOMER_BUY_ASSET || type == WalletTransactionType.CUSTOMER_SELL_ASSET) {
                            // Trừ phí cho CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
                            return -amount;
                        }
                        // Các loại giao dịch khác đóng góp số tiền như bình thường
                        return amount;
                    })
                    .sum();

            // Tạo timestamp cho ngày đầu tiên của tháng
            long timestamp = currentYearMonth.atDay(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;

            // Thêm vào kết quả
            result.add(Arrays.<Object>asList(timestamp, totalAmount));

            // Chuyển sang tháng tiếp theo
            currentYearMonth = currentYearMonth.plusMonths(1);
        }

        return result;
    }

    @Override
    public List<List<Object>> getTotalFeeAmountByMonthWithTimestamp(Long months, List<WalletTransactionType> transactionTypes) {
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

        // Tạo danh sách mới để bao gồm các loại phí
        List<WalletTransactionType> extendedTransactionTypes = new ArrayList<>(transactionTypes != null ? transactionTypes : Collections.emptyList());
        // Luôn thêm CUSTOMER_BUY_ASSET và CUSTOMER_SELL_ASSET
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_BUY_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_BUY_ASSET);
        }
        if (!extendedTransactionTypes.contains(WalletTransactionType.CUSTOMER_SELL_ASSET)) {
            extendedTransactionTypes.add(WalletTransactionType.CUSTOMER_SELL_ASSET);
        }

        // Lấy giao dịch từ repository
        List<WalletTransaction> transactions = walletTransactionRepository.findAllByDateBetweenAndTransactionTypes(startDate, endDate, extendedTransactionTypes);

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

            // Tính tổng phí trong tháng
            double totalFeeAmount = monthlyTransactions.stream()
                    .filter(tx -> tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_BUY_ASSET ||
                            tx.getWalletTransactionType() == WalletTransactionType.CUSTOMER_SELL_ASSET)
                    .mapToDouble(tx -> Math.abs(tx.getAmount().doubleValue()))
                    .sum();

            // Tạo timestamp cho ngày đầu tiên của tháng
            long timestamp = currentYearMonth.atDay(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;

            // Thêm vào kết quả
            result.add(Arrays.<Object>asList(timestamp, totalFeeAmount));

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
