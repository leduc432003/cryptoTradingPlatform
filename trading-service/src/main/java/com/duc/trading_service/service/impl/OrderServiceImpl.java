package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.CoinDTO;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.CoinService;
import com.duc.trading_service.service.OrderItemService;
import com.duc.trading_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CoinService coinService;
    private final OrderItemService orderItemService;

    @Override
    public Orders createOrder(Long userId, OrderItem orderItem, OrderType orderType) {
        double price = coinService.getCoinById(orderItem.getCoinId()).getCurrentPrice() * orderItem.getQuantity();
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderItem(orderItem);
        order.setOrderType(orderType);
        order.setPrice(BigDecimal.valueOf(price));
        order.setTimestamp(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    @Override
    public Orders getOrderById(Long orderId) throws Exception {
        return orderRepository.findById(orderId).orElseThrow(() -> new Exception("order not found"));
    }

    @Override
    public List<Orders> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Orders processOrder(String coinId, double quantity, OrderType orderType, Long userId) throws Exception {
        if(orderType == OrderType.BUY) {
            return buyAsset(coinId, quantity, userId);
        }
        throw new Exception("order type is invalid");
    }

    @Override
    @Transactional
    public Orders buyAsset(String coinId, double quantity, Long userId) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        double buyPrice = coinService.getCoinById(coinId).getCurrentPrice();
        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, 0);
        Orders order = createOrder(userId, orderItem, OrderType.BUY);
        orderItem.setOrder(order);
//        walletService.payOrderPayment(order);
        order.setStatus(OrderStatus.SUCCESS);
        order.setOrderType(OrderType.BUY);
        return orderRepository.save(order);
    }

    @Override
    public Orders sellAsset(String coinId, double quantity, Long userId) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }
        double sellPrice = coinService.getCoinById(coinId).getCurrentPrice();
        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, 0, sellPrice);
        Orders order = createOrder(userId, orderItem, OrderType.SELL);
        orderItem.setOrder(order);
//        walletService.payOrderPayment(order);
        order.setStatus(OrderStatus.SUCCESS);
        order.setOrderType(OrderType.SELL);
        return orderRepository.save(order);
    }
}
