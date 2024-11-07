package com.duc.trading_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssetRequest {
    private Long userId;
    private String coinId;
    private double quantity;
}
