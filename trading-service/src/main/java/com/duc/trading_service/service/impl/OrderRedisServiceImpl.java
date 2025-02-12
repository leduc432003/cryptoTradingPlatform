package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.CoinDTO;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.CoinService;
import com.duc.trading_service.service.OrderRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRedisServiceImpl implements OrderRedisService {
    private final OrderRepository orderRepository;
    private final CoinService coinService;

    @Override
    @Cacheable(value = "orders", key = "#tradingSymbol")
    public List<Orders> getOrderByStatusAndTradingSymbol(OrderStatus status, String tradingSymbol) {
        return orderRepository.findByStatusAndTradingSymbol(status, tradingSymbol);
    }

    @Override
    @CacheEvict(value = "orders", key = "#order.tradingSymbol")
    public void updateOrderStatus(Orders order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    @CacheEvict(value = "orders", key = "#order.tradingSymbol")
    public void updateOrderType(Orders order, OrderType orderType) {
        order.setOrderType(orderType);
        orderRepository.save(order);
    }

    @CacheEvict(value = "orders", key = "#result.tradingSymbol")
    @Override
    public Orders createOrder(Long userId, OrderItem orderItem, OrderType orderType) {
        CoinDTO coinDTO = coinService.getCoinById(orderItem.getCoinId());
        double price = coinDTO.getCurrentPrice() * orderItem.getQuantity();
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderItem(orderItem);
        order.setOrderType(orderType);
        order.setTradingSymbol(coinDTO.getTradingSymbol());

        if (orderType == OrderType.LIMIT_BUY || orderType == OrderType.LIMIT_SELL) {
            order.setPrice(BigDecimal.ZERO);
        } else {
            order.setPrice(BigDecimal.valueOf(price));
        }

        order.setTimestamp(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        Orders savedOrder = orderRepository.save(order);
        return savedOrder;
    }
}
