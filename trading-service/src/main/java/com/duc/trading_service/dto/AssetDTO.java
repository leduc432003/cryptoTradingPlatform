package com.duc.trading_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private Long id;
    private double quantity;
    private double buyPrice;
    private String coinId;
    private Long userId;
}
