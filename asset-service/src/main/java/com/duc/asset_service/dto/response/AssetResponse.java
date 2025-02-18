package com.duc.asset_service.dto.response;

import com.duc.asset_service.dto.CoinDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetResponse {
    private Long id;
    private double quantity;
    private double buyPrice;
    private Long userId;
    private CoinDTO coinDTO;
}
