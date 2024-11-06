package com.duc.trading_service.service;

import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderType;

import java.util.List;

public interface OrderService {
    Orders createOrder(Long userId, OrderItem orderItem, OrderType orderType);
    Orders getOrderById(Long orderId) throws Exception;
    List<Orders> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol);
    Orders processOrder(String coinId, double quantity, OrderType orderType, Long userId, String jwt) throws Exception;
    Orders buyAsset(String coinId, double quantity, Long userId, String jwt) throws Exception;
    Orders sellAsset(String coinId, double quantity, Long userId, String jwt) throws Exception;
}
