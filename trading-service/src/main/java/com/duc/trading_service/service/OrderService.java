package com.duc.trading_service.service;

import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Orders getOrderById(Long orderId) throws Exception;
    List<Orders> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol);
    void cancelLimitOrder(Long orderId, Long userId) throws Exception;
    Orders processOrder(String coinId, double quantity, BigDecimal stopPrice, BigDecimal limitPrice, OrderType orderType, Long userId, String jwt) throws Exception;
    void matchOrdersWithPrice(String symbol, BigDecimal currentPrice);
    List<Orders> getOrdersByStatus(Long userId, OrderStatus status);
    Page<Orders> getAllOrders(Pageable pageable);
}
