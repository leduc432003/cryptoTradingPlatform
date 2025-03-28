package com.duc.wallet_service.controller;

import com.duc.wallet_service.dto.UserDTO;
import com.duc.wallet_service.dto.UserRole;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;
import com.duc.wallet_service.service.UserService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WalletTransactionController {
    private final UserService userService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;

    @GetMapping
    public ResponseEntity<List<WalletTransaction>> getUserWalletTransaction(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(value = "days", required = false) Long days) {
        UserDTO user = userService.getUserProfile(jwt);
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        List<WalletTransaction> walletTransactions = walletTransactionService.getWalletTransactionsByWalletIdAndDays(wallet.getId(), days);
        return new ResponseEntity<>(walletTransactions, HttpStatus.OK);
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<List<WalletTransaction>> getUserWalletTransactionAdmin(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long userId,
            @RequestParam(value = "days", required = false) Long days) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        Wallet wallet = walletService.getWalletByUserId(userId);
        List<WalletTransaction> walletTransactions = walletTransactionService.getWalletTransactionsByWalletIdAndDays(wallet.getId(), days);
        return new ResponseEntity<>(walletTransactions, HttpStatus.OK);
    }

    @GetMapping("/admin/transaction")
    public ResponseEntity<List<WalletTransaction>> getAllTransaction(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "transaction_type", required = false) List<WalletTransactionType> transactionTypes,
            @RequestParam(value = "days", required = false) Long days) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }

        if (days != null) {
            LocalDate endDateNow = LocalDate.now();
            startDate = endDateNow.minusDays(days);
            endDate = endDateNow;
        } else {
            if (startDate == null) {
                startDate = LocalDate.MIN;
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
        }

        List<WalletTransaction> walletTransactionList = walletTransactionService.getTransactionsByFilters(startDate, endDate, transactionTypes);
        return new ResponseEntity<>(walletTransactionList, HttpStatus.OK);
    }

    @GetMapping("/admin/total-amount-transaction-by-range")
    public ResponseEntity<Double> getTotalAmountTransactionByRange(@RequestHeader("Authorization") String jwt,
                                                                     @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                   @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                   @RequestParam(value = "transaction_type", required = false) List<WalletTransactionType> transactionTypes) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }

        if (startDate == null) {
            startDate = LocalDate.MIN;
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        double totalAmount = walletTransactionService.getTotalAmountByDateRange(startDate, endDate, transactionTypes);

        return new ResponseEntity<>(totalAmount, HttpStatus.OK);
    }

    @GetMapping("/admin/total-amount-transaction")
    public ResponseEntity<Double> getTotalAmountTransaction(@RequestHeader("Authorization") String jwt,
                                                            @RequestParam(value = "days", required = false) Long days,
                                                            @RequestParam(value = "transaction_type", required = false) List<WalletTransactionType> transactionTypes) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        return new ResponseEntity<>(walletTransactionService.getTotalAmountByFilters(days, transactionTypes), HttpStatus.OK);
    }

    @GetMapping("/admin/total-volume/chart")
    public ResponseEntity<List<List<Object>>> getTotalVolumeChart(@RequestHeader("Authorization") String jwt,
                                                            @RequestParam(value = "days", required = false) Long days,
                                                                  @RequestParam(value = "transaction_type", required = false) List<WalletTransactionType> transactionTypes) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        List<List<Object>> totalVolume = walletTransactionService.getTotalAmountByDateWithTimestamp(days, transactionTypes);

        return ResponseEntity.ok(totalVolume);
    }

    @GetMapping("/admin/total-volume-by-month/chart")
    public ResponseEntity<List<List<Object>>> getTotalVolumeByMonthChart(@RequestHeader("Authorization") String jwt,
                                                                  @RequestParam(value = "months", required = false) Long months,
                                                                  @RequestParam(value = "transaction_type", required = false) List<WalletTransactionType> transactionTypes) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can see wallet user");
        }
        List<List<Object>> totalVolume = walletTransactionService.getTotalAmountByMonthWithTimestamp(months, transactionTypes);

        return ResponseEntity.ok(totalVolume);
    }
}
