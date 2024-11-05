package com.duc.trading_service.service;

import com.duc.trading_service.model.OrderItem;

public interface OrderItemService {
    OrderItem createOrderItem(String coinId, double quantity, double buyPrice, double sellPrice);
}
