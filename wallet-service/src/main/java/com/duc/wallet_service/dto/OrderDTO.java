package com.duc.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal limitPrice;
    private BigDecimal stopPrice;
    private LocalDateTime timestamp = LocalDateTime.now();
    private OrderStatus status;
    private OrderItem orderItem;
}
