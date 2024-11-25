package com.duc.trading_service.dto.request;

import com.duc.trading_service.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private String coinId;
    private double quantity;
    private OrderType orderType;
    private double limitPrice = 0.0;
    private double stopPrice = 0.0;
}
