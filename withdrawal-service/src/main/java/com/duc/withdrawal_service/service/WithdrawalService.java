package com.duc.withdrawal_service.service;

import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;

import java.util.List;

public interface WithdrawalService {
    Withdrawal requestWithdrawal(String jwt, Long amount, Long userId) throws Exception;
    Withdrawal proceedWithdrawal(Long withdrawalId, boolean accept) throws Exception;
    List<Withdrawal> getUsersWithdrawalHistory(Long userId);
    List<Withdrawal> getAllWithdrawalRequest(WithdrawalStatus withdrawalStatus);
}
