package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.AssetDTO;
import com.duc.trading_service.dto.CoinDTO;
import com.duc.trading_service.dto.request.CreateAssetRequest;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.*;
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
    private final WalletService walletService;
    private final AssetService assetService;

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
    public Orders processOrder(String coinId, double quantity, OrderType orderType, Long userId, String jwt) throws Exception {
        if(orderType == OrderType.BUY) {
            return buyAsset(coinId, quantity, userId, jwt);
        } else if (orderType == OrderType.SELL) {
            return sellAsset(coinId, quantity, userId, jwt);
        }
        throw new Exception("order type is invalid");
    }

    @Override
    @Transactional
    public Orders buyAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }
        double buyPrice = coinService.getCoinById(coinId).getCurrentPrice();
        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, 0);
        Orders order = createOrder(userId, orderItem, OrderType.BUY);
        orderItem.setOrder(order);
        walletService.payOrderPayment(jwt, order.getId());
        order.setStatus(OrderStatus.SUCCESS);
        order.setOrderType(OrderType.BUY);

        AssetDTO oldAsset = assetService.getAssetByUserIdAndCoinId(jwt, coinId);

        if(oldAsset == null) {
            CreateAssetRequest request = new CreateAssetRequest();
            request.setCoinId(coinId);
            request.setQuantity(quantity);
            assetService.createAsset(jwt, request);
        } else {
            assetService.updateAsset(jwt, oldAsset.getId(), quantity);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Orders sellAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }
        double sellPrice = coinService.getCoinById(coinId).getCurrentPrice();
        AssetDTO assetToSell = assetService.getAssetByUserIdAndCoinId(jwt, coinId);

        if(assetToSell != null) {
            double buyPrice = assetToSell.getBuyPrice();
            OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, sellPrice);
            Orders order = createOrder(userId, orderItem, OrderType.SELL);
            orderItem.setOrder(order);
            if(assetToSell.getQuantity() >= quantity) {
                order.setStatus(OrderStatus.SUCCESS);
                order.setOrderType(OrderType.SELL);
                Orders saveOrder = orderRepository.save(order);
                walletService.payOrderPayment(jwt, order.getId());
                AssetDTO updateAsset = assetService.updateAsset(jwt, assetToSell.getId(), -quantity);
                if(updateAsset.getQuantity() * sellPrice <= 0) {
                    assetService.deleteAsset(jwt, updateAsset.getId());
                }
                return saveOrder;
            }
            throw new Exception("Insufficient quantity to sell");
        }
        throw new Exception("Asset not found");
    }
}
