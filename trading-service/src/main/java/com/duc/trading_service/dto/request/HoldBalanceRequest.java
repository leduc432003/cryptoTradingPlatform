package com.duc.trading_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HoldBalanceRequest {
    private Long userId;
    private double money;
}
