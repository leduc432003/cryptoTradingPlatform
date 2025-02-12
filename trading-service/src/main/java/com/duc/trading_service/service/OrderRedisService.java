package com.duc.trading_service.service;

import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.model.Orders;

import java.util.List;

public interface OrderRedisService {
    List<Orders> getOrderByStatusAndTradingSymbol(OrderStatus status, String tradingSymbol);
    void updateOrderStatus(Orders order, OrderStatus newStatus);
    void updateOrderType(Orders order, OrderType orderType);
    Orders createOrder(Long userId, OrderItem orderItem, OrderType orderType);
}
