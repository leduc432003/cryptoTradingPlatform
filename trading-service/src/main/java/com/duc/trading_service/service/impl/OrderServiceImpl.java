package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.AssetDTO;
import com.duc.trading_service.dto.request.CreateAssetRequest;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
    @Value("${internal.service.token}")
    private String internalServiceToken;

    @Override
    public Orders createOrder(Long userId, OrderItem orderItem, OrderType orderType) {
        double price = coinService.getCoinById(orderItem.getCoinId()).getCurrentPrice() * orderItem.getQuantity();
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderItem(orderItem);
        order.setOrderType(orderType);

        // Nếu là LIMIT_BUY hoặc LIMIT_SELL, thì sử dụng limitPrice thay vì price hiện tại
        if (orderType == OrderType.LIMIT_BUY || orderType == OrderType.LIMIT_SELL) {
            order.setPrice(BigDecimal.valueOf(price)); // Có thể giữ giá trị hiện tại hoặc tính lại giá cho LIMIT (nếu cần)
            order.setLimitPrice(BigDecimal.valueOf(price)); // Đặt limitPrice cho đơn hàng LIMIT_*
        } else {
            order.setPrice(BigDecimal.valueOf(price)); // Đặt giá hiện tại cho các đơn hàng không phải LIMIT
        }

        // Thiết lập các thuộc tính khác
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
        List<Orders> ordersList =  orderRepository.findByUserId(userId);
        return ordersList.stream().filter(orders -> (orderType == null || orders.getOrderType().name().equalsIgnoreCase(orderType.toString())) || (assetSymbol == null || orders.getOrderItem().getCoinId().equalsIgnoreCase(assetSymbol))).toList();
    }

    @Override
    public Orders processOrder(String coinId, double quantity, BigDecimal limitPrice, OrderType orderType, Long userId, String jwt) throws Exception {
        return switch (orderType) {
            case BUY -> buyAsset(coinId, quantity, userId, jwt);
            case SELL -> sellAsset(coinId, quantity, userId, jwt);
            case LIMIT_BUY -> placeLimitOrder(coinId, quantity, userId, limitPrice, OrderType.LIMIT_BUY);
            case LIMIT_SELL -> placeLimitOrder(coinId, quantity, userId, limitPrice, OrderType.LIMIT_SELL);
            default -> throw new Exception("Invalid order type");
        };
    }

    private Orders placeLimitOrder(String coinId, double quantity, Long userId, BigDecimal limitPrice, OrderType orderType) throws Exception {
        if (quantity <= 0 || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantity and limit price must be greater than 0");
        }

        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, 0, 0);
        Orders limitOrder = createOrder(userId, orderItem, OrderType.LIMIT_BUY);
        return limitOrder;
    }

    public void matchLimitOrders() {
        List<Orders> pendingLimitOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Orders order : pendingLimitOrders) {
            BigDecimal currentPrice = BigDecimal.valueOf(coinService.getCoinById(order.getOrderItem().getCoinId()).getCurrentPrice());

            if ((order.getOrderType() == OrderType.LIMIT_BUY && currentPrice.compareTo(order.getLimitPrice()) <= 0) ||
                    (order.getOrderType() == OrderType.LIMIT_SELL && currentPrice.compareTo(order.getLimitPrice()) >= 0)) {

                try {
                    if (order.getOrderType() == OrderType.LIMIT_BUY) {
                        buyAsset(order.getOrderItem().getCoinId(), order.getOrderItem().getQuantity(), order.getUserId(), "internal");
                    } else if (order.getOrderType() == OrderType.LIMIT_SELL) {
                        sellAsset(order.getOrderItem().getCoinId(), order.getOrderItem().getQuantity(), order.getUserId(), "internal");
                    }

                    order.setStatus(OrderStatus.SUCCESS);
                    orderRepository.save(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Transactional
    private Orders buyAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
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
            request.setUserId(userId);
            request.setCoinId(coinId);
            request.setQuantity(quantity);
            assetService.createAsset(internalServiceToken, request);
        } else {
            assetService.updateAsset(internalServiceToken, oldAsset.getId(), quantity);
        }

        return orderRepository.save(order);
    }
    
    @Transactional
    private Orders sellAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
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
                AssetDTO updateAsset = assetService.updateAsset(internalServiceToken, assetToSell.getId(), -quantity);
                if(updateAsset.getQuantity() * sellPrice <= 0) {
                    assetService.deleteAsset(internalServiceToken, updateAsset.getId());
                }
                return saveOrder;
            }
            throw new Exception("Insufficient quantity to sell");
        }
        throw new Exception("Asset not found");
    }
}
