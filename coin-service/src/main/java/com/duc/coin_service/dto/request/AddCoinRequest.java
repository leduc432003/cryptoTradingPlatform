package com.duc.coin_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddCoinRequest {
    private String coinId;
    private double minimumBuyPrice;
    private double transactionFee;
    private String tradingSymbol;
}
