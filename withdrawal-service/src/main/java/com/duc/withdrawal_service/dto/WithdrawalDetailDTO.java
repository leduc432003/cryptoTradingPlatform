package com.duc.withdrawal_service.dto;

import com.duc.withdrawal_service.model.Withdrawal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalDetailDTO {
    private Withdrawal withdrawal;
    private String bankAccount;
    private String bankName;
    private String accountHolderName;
}
