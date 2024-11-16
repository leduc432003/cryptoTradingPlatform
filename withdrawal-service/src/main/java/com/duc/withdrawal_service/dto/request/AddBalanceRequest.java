package com.duc.withdrawal_service.dto.request;

import com.duc.withdrawal_service.dto.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddBalanceRequest {
    private Long userId;
    private double money;
    private WalletTransactionType transactionType;
}
