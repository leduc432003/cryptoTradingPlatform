package com.duc.wallet_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private double quantity;
    private String coinId;
    private double buyPrice;
    private double sellPrice;
    @JsonIgnore
    private OrderDTO order;
}
