package com.duc.trading_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WalletDTO {
    private Long id;
    private Long userId;
    private BigDecimal balance;
}
