package com.duc.withdrawal_service.service.impl;

import com.duc.withdrawal_service.dto.PaymentDetailsDTO;
import com.duc.withdrawal_service.dto.WalletDTO;
import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;
import com.duc.withdrawal_service.repository.WithdrawalRepository;
import com.duc.withdrawal_service.service.PaymentDetailsService;
import com.duc.withdrawal_service.service.WalletService;
import com.duc.withdrawal_service.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;
    private final PaymentDetailsService paymentDetailsService;
    private final WalletService walletService;

    @Override
    public Withdrawal requestWithdrawal(String jwt, Long amount, Long userId) throws Exception {
        PaymentDetailsDTO paymentDetails = paymentDetailsService.getUserPaymentDetails(jwt);
        if (paymentDetails == null) {
            throw new Exception("User does not have a linked payment account.");
        }
        WalletDTO walletDTO = walletService.getUserWallet(jwt);
        BigDecimal balance = walletDTO.getBalance();
        BigDecimal withdrawalAmount = BigDecimal.valueOf(amount);
        if(balance.compareTo(withdrawalAmount) < 0) {
            throw new Exception("Insufficient wallet balance for withdrawal.");
        }
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setUserId(userId);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public Withdrawal proceedWithdrawal(Long withdrawalId, boolean accept) throws Exception {
        Optional<Withdrawal> withdrawal = withdrawalRepository.findById(withdrawalId);
        if(withdrawal.isEmpty()) {
            throw new Exception("Withdrawal not found");
        }
        Withdrawal approveWithdrawal = withdrawal.get();
        approveWithdrawal.setDateTime(LocalDateTime.now());
        if(accept) {
            approveWithdrawal.setStatus(WithdrawalStatus.SUCCESS);
        } else {
            approveWithdrawal.setStatus(WithdrawalStatus.DECLINE);
        }
        return withdrawalRepository.save(approveWithdrawal);
    }

    @Override
    public List<Withdrawal> getUsersWithdrawalHistory(Long userId) {
        return withdrawalRepository.findByUserId(userId);
    }

    @Override
    public List<Withdrawal> getAllWithdrawalRequest(WithdrawalStatus withdrawalStatus) {
        List<Withdrawal> withdrawalList = withdrawalRepository.findAll();
        List<Withdrawal> filteredWithdrawal = withdrawalList.stream()
                .filter(withdrawal -> withdrawalStatus == null ||
                        withdrawal.getStatus().name().equalsIgnoreCase(withdrawalStatus.toString()))
                .toList();
        return filteredWithdrawal;
    }
}
