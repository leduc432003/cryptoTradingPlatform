package com.duc.withdrawal_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentDetailsDTO {
    private Long id;
    private String accountNumber;
    private String accountName;
    private String bankName;
    private Long userId;
}
