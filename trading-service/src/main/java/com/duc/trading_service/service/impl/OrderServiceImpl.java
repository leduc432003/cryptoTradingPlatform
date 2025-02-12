package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.AssetDTO;
import com.duc.trading_service.dto.CoinDTO;
import com.duc.trading_service.dto.WalletDTO;
import com.duc.trading_service.dto.WalletTransactionType;
import com.duc.trading_service.dto.request.AddBalanceRequest;
import com.duc.trading_service.dto.request.CreateAssetRequest;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CoinService coinService;
    private final OrderItemService orderItemService;
    private final WalletService walletService;
    private final AssetService assetService;
    private final OrderRedisService orderRedisService;
    @Value("${internal.service.token}")
    private String internalServiceToken;
    @Value("${internal1.service.token}")
    private String internal1ServiceToken;

    @Override
    public Orders getOrderById(Long orderId) throws Exception {
        return orderRepository.findById(orderId).orElseThrow(() -> new Exception("order not found"));
    }

    @Override
    public List<Orders> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol) {
        List<Orders> ordersList =  orderRepository.findByUserId(userId);
        return ordersList.stream()
                .filter(orders -> orderType == null || orders.getOrderType().name().equalsIgnoreCase(orderType.toString()))
                .filter(order -> assetSymbol == null || order.getOrderItem().getCoinId().equalsIgnoreCase(assetSymbol))
                .toList();
    }

    @Override
    public Orders processOrder(String coinId, double quantity, BigDecimal stopPrice, BigDecimal limitPrice, OrderType orderType, Long userId, String jwt) throws Exception {
        return switch (orderType) {
            case BUY -> buyAsset(coinId, quantity, userId, jwt);
            case SELL -> sellAsset(coinId, quantity, userId, jwt);
            case LIMIT_BUY -> placeLimitOrder(coinId, quantity, userId, limitPrice, OrderType.LIMIT_BUY,  jwt);
            case LIMIT_SELL -> placeLimitOrder(coinId, quantity, userId, limitPrice, OrderType.LIMIT_SELL, jwt);
            case STOP_LIMIT_BUY -> placeStopLimitOrder(coinId, quantity, userId, stopPrice, limitPrice, OrderType.STOP_LIMIT_BUY, jwt);
            case STOP_LIMIT_SELL -> placeStopLimitOrder(coinId, quantity, userId, stopPrice, limitPrice, OrderType.STOP_LIMIT_SELL, jwt);
            default -> throw new Exception("Invalid order type");
        };
    }

    @Override
    @Transactional
    public void matchOrdersWithPrice(String symbol, BigDecimal currentPrice) {
        List<Orders> ordersList = orderRedisService.getOrderByStatusAndTradingSymbol(OrderStatus.PENDING, symbol.toLowerCase());
        System.out.println(ordersList);
        for (Orders order : ordersList) {
            if (order.getOrderItem() == null) {
                continue;
            }

            if (order.getOrderType() == OrderType.STOP_LIMIT_BUY && currentPrice.compareTo(order.getStopPrice()) >= 0) {
                order.setOrderType(OrderType.LIMIT_BUY);
                orderRepository.save(order);
            } else if (order.getOrderType() == OrderType.STOP_LIMIT_SELL && currentPrice.compareTo(order.getStopPrice()) <= 0) {
                order.setOrderType(OrderType.LIMIT_SELL);
                orderRepository.save(order);
            }

            if ((order.getOrderType() == OrderType.LIMIT_BUY && currentPrice.compareTo(order.getLimitPrice()) <= 0) ||
                    (order.getOrderType() == OrderType.LIMIT_SELL && currentPrice.compareTo(order.getLimitPrice()) >= 0)) {
                System.out.println("mua dc r");
                try {
                    if (order.getOrderType() == OrderType.LIMIT_BUY) {
                        buyAssetForLimitOrder(order, internal1ServiceToken);
                    } else if (order.getOrderType() == OrderType.LIMIT_SELL) {
                        sellAssetForLimitOrder(order, internal1ServiceToken);
                    }

                    orderRedisService.updateOrderStatus(order, OrderStatus.SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Transactional
    private Orders placeStopLimitOrder(String coinId, double quantity, Long userId, BigDecimal stopPrice, BigDecimal limitPrice, OrderType orderType, String jwt) throws Exception {
        if (quantity <= 0 || stopPrice.compareTo(BigDecimal.ZERO) <= 0 || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantity, stop price, and limit price must be greater than 0");
        }

        if (orderType == OrderType.STOP_LIMIT_SELL) {
            AssetDTO currentAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, userId);
            if (currentAsset == null || currentAsset.getQuantity() < quantity) {
                throw new Exception("Insufficient assets to sell");
            }
        } else if (orderType == OrderType.STOP_LIMIT_BUY) {
            WalletDTO walletDTO = walletService.getUserWallet(jwt);
            if (walletDTO.getBalance().compareTo(limitPrice.multiply(BigDecimal.valueOf(quantity))) < 0) {
                throw new Exception("Insufficient balance to place order");
            }
        }

        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, stopPrice.doubleValue(), limitPrice.doubleValue());
        Orders order = orderRedisService.createOrder(userId, orderItem, orderType);
        order.setStopPrice(stopPrice);
        order.setLimitPrice(limitPrice);
        order.setStatus(OrderStatus.PENDING);
        orderItem.setOrder(order);
        return orderRepository.save(order);
    }


    @Transactional
    private Orders placeLimitOrder(String coinId, double quantity, Long userId, BigDecimal limitPrice, OrderType orderType, String jwt) throws Exception {
        if (quantity <= 0 || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantity and limit price must be greater than 0");
        }

        if(orderType == OrderType.LIMIT_SELL) {
            AssetDTO currentAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, userId);
            if (currentAsset == null || currentAsset.getQuantity() < quantity) {
                throw new Exception("Insufficient assets to sell");
            }
        } else if (orderType == OrderType.LIMIT_BUY) {
            WalletDTO walletDTO = walletService.getUserWallet(jwt);
            if(walletDTO.getBalance().compareTo(BigDecimal.valueOf(limitPrice.doubleValue() * quantity)) < 0) {
                throw new Exception("Insufficient balance to buy");
            }
        }

        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, 0, 0);
        Orders order = orderRedisService.createOrder(userId, orderItem, orderType);
        order.setLimitPrice(limitPrice);
        order.setStatus(OrderStatus.PENDING);
        orderItem.setOrder(order);
        return orderRepository.save(order);
    }

    @Transactional
    private void buyAssetForLimitOrder(Orders limitOrder, String jwt) throws Exception {
        if (limitOrder == null || limitOrder.getOrderItem() == null) {
            throw new Exception("Invalid limit order");
        }

        double quantity = limitOrder.getOrderItem().getQuantity();
        if (quantity <= 0) {
            throw new Exception("Quantity must be greater than 0");
        }

        double buyPrice = limitOrder.getLimitPrice().doubleValue();
        String coinId = limitOrder.getOrderItem().getCoinId();

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setMoney(-buyPrice * quantity);
        addBalanceRequest.setUserId(limitOrder.getUserId());
        addBalanceRequest.setTransactionType(WalletTransactionType.BUY_ASSET);
        walletService.addBalance(jwt, addBalanceRequest);

        limitOrder.setStatus(OrderStatus.SUCCESS);

        AssetDTO oldAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, limitOrder.getOrderItem().getCoinId(), limitOrder.getUserId());
        if (oldAsset == null) {
            CreateAssetRequest request = new CreateAssetRequest();
            request.setUserId(limitOrder.getUserId());
            request.setCoinId(coinId);
            request.setQuantity(quantity);
            assetService.createAsset(internalServiceToken, request);
        } else {
            assetService.updateAsset(internalServiceToken, oldAsset.getId(), quantity);
        }

        orderRepository.save(limitOrder);
    }

    @Transactional
    private void sellAssetForLimitOrder(Orders limitOrder, String jwt) throws Exception {
        if (limitOrder == null || limitOrder.getOrderItem() == null) {
            throw new Exception("Invalid limit order");
        }

        BigDecimal sellPrice = limitOrder.getLimitPrice();
        AssetDTO oldAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, limitOrder.getOrderItem().getCoinId(), limitOrder.getUserId());
        AssetDTO updatedAsset = assetService.updateAsset(internalServiceToken, oldAsset.getId(), -limitOrder.getOrderItem().getQuantity());
        if (updatedAsset.getQuantity() <= 0) {
            assetService.deleteAsset(internalServiceToken, updatedAsset.getId());
        }

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(limitOrder.getUserId());
        addBalanceRequest.setMoney(sellPrice.doubleValue() * limitOrder.getOrderItem().getQuantity());
        addBalanceRequest.setTransactionType(WalletTransactionType.SELL_ASSET);
        walletService.addBalance(jwt, addBalanceRequest);
        orderRepository.save(limitOrder);
    }

    @Transactional
    private Orders buyAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }
        double buyPrice = coinService.getCoinById(coinId).getCurrentPrice();
        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, 0);
        Orders order = orderRedisService.createOrder(userId, orderItem, OrderType.BUY);
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
            Orders order = orderRedisService.createOrder(userId, orderItem, OrderType.SELL);
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

    @Transactional
    @Override
    public void cancelLimitOrder(Long orderId, Long userId) throws Exception {
        Orders order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new Exception("You are not authorized to cancel this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new Exception("Order cannot be canceled as it is not in PENDING status");
        }

        orderRedisService.updateOrderStatus(order, OrderStatus.CANCELLED);
    }
}
