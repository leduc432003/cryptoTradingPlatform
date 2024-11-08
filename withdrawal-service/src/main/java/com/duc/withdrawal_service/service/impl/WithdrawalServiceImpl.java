package com.duc.withdrawal_service.service.impl;

import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;
import com.duc.withdrawal_service.repository.WithdrawalRepository;
import com.duc.withdrawal_service.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;

    @Override
    public Withdrawal requestWithdrawal(Long amount, Long userId) {
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
    public List<Withdrawal> getAllWithdrawalRequest() {
        return withdrawalRepository.findAll();
    }
}
