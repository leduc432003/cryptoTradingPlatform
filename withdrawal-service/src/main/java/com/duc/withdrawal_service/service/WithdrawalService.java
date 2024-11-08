package com.duc.withdrawal_service.service;

import com.duc.withdrawal_service.model.Withdrawal;

import java.util.List;

public interface WithdrawalService {
    Withdrawal requestWithdrawal(Long amount, Long userId);
    Withdrawal proceedWithdrawal(Long withdrawalId, boolean accept) throws Exception;
    List<Withdrawal> getUsersWithdrawalHistory(Long userId);
    List<Withdrawal> getAllWithdrawalRequest();
}
