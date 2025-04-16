package com.duc.withdrawal_service.controller;

import com.duc.withdrawal_service.dto.*;
import com.duc.withdrawal_service.dto.request.AddBalanceRequest;
import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;
import com.duc.withdrawal_service.service.PaymentDetailsService;
import com.duc.withdrawal_service.service.UserService;
import com.duc.withdrawal_service.service.WalletService;
import com.duc.withdrawal_service.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawal")
public class WithdrawalController {
    private final WithdrawalService withdrawalService;
    private final UserService userService;
    private final WalletService walletService;
    private final PaymentDetailsService paymentDetailsService;
    @Value("${internal.service.token}")
    private String internalServiceToken;

    @PostMapping
    public ResponseEntity<Withdrawal> withdrawalRequest(@RequestHeader("Authorization") String jwt, @RequestParam Long amount) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Withdrawal withdrawal = withdrawalService.requestWithdrawal(jwt, amount, user.getId());
        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(user.getId());
        addBalanceRequest.setMoney(-withdrawal.getAmount());
        addBalanceRequest.setTransactionType(WalletTransactionType.WITHDRAWAL);
        walletService.addBalance(internalServiceToken, addBalanceRequest);
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @PatchMapping("/{id}/proceed")
    public ResponseEntity<Withdrawal> proceedWithdrawal(@RequestHeader("Authorization") String jwt, @PathVariable Long id, @RequestParam boolean accept) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(!user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new Exception("Only admin can proceed withdrawal");
        }
        Withdrawal withdrawal = withdrawalService.proceedWithdrawal(id, accept);
        if(!accept) {
            AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
            addBalanceRequest.setUserId(withdrawal.getUserId());
            addBalanceRequest.setMoney(withdrawal.getAmount());
            addBalanceRequest.setTransactionType(WalletTransactionType.REFUND_WITHDRAWAL);
            walletService.addBalance(internalServiceToken, addBalanceRequest);
        }
        return new ResponseEntity<>(withdrawal, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Withdrawal>> getWithdrawalHistory(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Withdrawal> withdrawalList = withdrawalService.getUsersWithdrawalHistory(user.getId());

        return new ResponseEntity<>(withdrawalList, HttpStatus.OK);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<WithdrawalDetailDTO>> getAllWithdrawalHistory(@RequestHeader("Authorization") String jwt, @RequestParam(required = false) WithdrawalStatus withdrawalStatus) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(!user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new Exception("Only admin can watch withdrawal");
        }
        List<Withdrawal> withdrawalList = withdrawalService.getAllWithdrawalRequest(withdrawalStatus);

        Map<Long, PaymentDetailsDTO> paymentDetailsCache = new HashMap<>();
        List<WithdrawalDetailDTO> withdrawalDetails = withdrawalList.stream().map(withdrawal -> {
            PaymentDetailsDTO bankAccount = paymentDetailsCache.computeIfAbsent(withdrawal.getUserId(),
                    userId -> paymentDetailsService.getUserPaymentDetailsById(jwt, userId));

            WithdrawalDetailDTO detail = new WithdrawalDetailDTO();
            detail.setWithdrawal(withdrawal);

            if (bankAccount != null) {
                detail.setBankAccount(bankAccount.getAccountNumber());
                detail.setBankName(bankAccount.getBankName());
                detail.setAccountHolderName(bankAccount.getAccountName());
            } else {
                detail.setBankAccount("N/A");
                detail.setBankName("N/A");
                detail.setAccountHolderName("N/A");
            }

            return detail;
        }).toList();


        return new ResponseEntity<>(withdrawalDetails, HttpStatus.OK);
    }

    @GetMapping("/admin/{withdrawalId}")
    public ResponseEntity<WithdrawalDetailDTO> getWithdrawalById(@RequestHeader("Authorization") String jwt, @PathVariable Long withdrawalId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(!user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new Exception("Only admin can watch withdrawal");
        }
        Withdrawal withdrawal = withdrawalService.getWithdrawalById(withdrawalId);
        PaymentDetailsDTO bankAccount = paymentDetailsService.getUserPaymentDetailsById(jwt, withdrawal.getUserId());
        WithdrawalDetailDTO withdrawalDetail = new WithdrawalDetailDTO();
        withdrawalDetail.setWithdrawal(withdrawal);
        if (bankAccount != null) {
            withdrawalDetail.setBankAccount(bankAccount.getAccountNumber());
            withdrawalDetail.setBankName(bankAccount.getBankName());
            withdrawalDetail.setAccountHolderName(bankAccount.getAccountName());
        }
        return new ResponseEntity<>(withdrawalDetail, HttpStatus.OK);
    }
}