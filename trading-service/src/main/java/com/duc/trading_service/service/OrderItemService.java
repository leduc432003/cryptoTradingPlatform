package com.duc.trading_service.service;

import com.duc.trading_service.model.OrderItem;

import java.time.LocalDate;
import java.util.Map;

public interface OrderItemService {
    OrderItem createOrderItem(String coinId, double quantity, double buyPrice, double sellPrice);
    Map<String, Double> getTotalTransactionsByCoinInDateRange(LocalDate startDate, LocalDate endDate);
}
