package com.duc.coin_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddCoinRequest {
    private String coinId;
    private double minimumBuyPrice;
}
