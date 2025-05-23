package com.duc.trading_service.service.impl;

import com.duc.trading_service.dto.*;
import com.duc.trading_service.dto.request.AddBalanceRequest;
import com.duc.trading_service.dto.request.CreateAssetRequest;
import com.duc.trading_service.dto.request.HoldBalanceRequest;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.repository.OrderRepository;
import com.duc.trading_service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CoinService coinService;
    private final OrderItemService orderItemService;
    private final WalletService walletService;
    private final AssetService assetService;
    private final OrderRedisService orderRedisService;
    private final UserService userService;
    @Value("${internal.service.token}")
    private String internalServiceToken;
    @Value("${internal1.service.token}")
    private String internal1ServiceToken;
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Override
    public Orders getOrderById(Long orderId) throws Exception {
        return orderRepository.findById(orderId).orElseThrow(() -> new Exception("order not found"));
    }

    @Override
    public Page<Orders> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol, Integer days, OrderStatus status, Pageable pageable) {
        LocalDateTime startDate = (days != null) ? LocalDateTime.now().minusDays(days) : null;
        return orderRepository.findOrdersByUserIdAndFilters(userId, orderType, assetSymbol, startDate, status, pageable);
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
        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        for (Orders order : ordersList) {
            if (order.getOrderItem() == null) {
                continue;
            }
            try {
                OrderType orderType = order.getOrderType();
                BigDecimal stopPrice = order.getStopPrice();
                BigDecimal limitPrice = order.getLimitPrice();

                if (orderType == OrderType.STOP_LIMIT_BUY && currentPrice.compareTo(stopPrice) >= 0) {
                    order.setOrderType(OrderType.LIMIT_BUY);
                    orderRepository.save(order);
                    orderRedisService.updateOrderType(order, OrderType.LIMIT_BUY);
                } else if (orderType == OrderType.STOP_LIMIT_SELL && currentPrice.compareTo(stopPrice) <= 0) {
                    order.setOrderType(OrderType.LIMIT_SELL);
                    orderRepository.save(order);
                    orderRedisService.updateOrderType(order, OrderType.LIMIT_SELL);
                }

                boolean isLimitBuyMatch = orderType == OrderType.LIMIT_BUY && currentPrice.compareTo(limitPrice) <= 0;
                boolean isLimitSellMatch = orderType == OrderType.LIMIT_SELL && currentPrice.compareTo(limitPrice) >= 0;

                if (isLimitBuyMatch || isLimitSellMatch) {
                    if (isLimitBuyMatch) {
                        buyAssetForLimitOrder(order, internal1ServiceToken);
                    } else {
                        sellAssetForLimitOrder(order, internal1ServiceToken);
                    }
                    order.setStatus(OrderStatus.SUCCESS);
                    orderRepository.save(order);
                    orderRedisService.updateOrderStatus(order, OrderStatus.SUCCESS);
                    log.info("Khớp lệnh {}: loại={}, mã={}, giá={}",
                            order.getId(), orderType, symbol, currentPrice);
                }
            } catch (Exception e) {
                log.error("Không thể khớp lệnh {} cho mã {}: {}", order.getId(), symbol, e.getMessage());
            }
        }
    }

    @Override
    public List<Orders> getOrdersByStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Page<Orders> getAllOrders(OrderType orderType, String assetSymbol, Integer days, OrderStatus status, Pageable pageable) {
        LocalDateTime startDate = (days != null) ? LocalDateTime.now().minusDays(days) : null;
        return orderRepository.findAllWithFilters(orderType, assetSymbol, startDate, status, pageable);
    }

    @Transactional
    private Orders placeStopLimitOrder(String coinId, double quantity, Long userId, BigDecimal stopPrice, BigDecimal limitPrice, OrderType orderType, String jwt) throws Exception {
        if (quantity <= 0 || stopPrice.compareTo(BigDecimal.ZERO) <= 0 || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantity, stop price, and limit price must be greater than 0");
        }

        CoinDTO coinDTO = coinService.getCoinById(coinId);
        BigDecimal buyPrice = limitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal transactionFee = buyPrice.multiply(coinDTO.getTransactionFee());
        OrderItem orderItem = null;
        if (orderType == OrderType.STOP_LIMIT_SELL) {
            AssetDTO currentAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, userId);
            if (currentAsset == null || BigDecimal.valueOf(currentAsset.getQuantity()).compareTo(BigDecimal.valueOf(quantity)) < 0) {
                throw new Exception("Insufficient assets to sell");
            }
            AssetDTO updatedAsset = assetService.updateAsset(internalServiceToken, currentAsset.getId(), -quantity);
            if (updatedAsset.getQuantity() <= 0) {
                assetService.deleteAsset(internalServiceToken, updatedAsset.getId());
            }
            orderItem = orderItemService.createOrderItem(coinId, quantity, updatedAsset.getBuyPrice(), limitPrice.doubleValue());
        } else if (orderType == OrderType.STOP_LIMIT_BUY) {
            UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
            AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, admin.getId());

            double maxBuyQuantity = adminAsset.getQuantity() * 0.1;
            if(quantity > maxBuyQuantity) {
                throw new Exception("You can only buy up to " + maxBuyQuantity + " coins per transaction.");
            }

            WalletDTO walletDTO = walletService.getUserWallet(jwt);
            if (walletDTO.getBalance().compareTo(buyPrice.add(transactionFee)) < 0) {
                throw new Exception("Insufficient balance to place order");
            }

            HoldBalanceRequest holdBalanceRequest = new HoldBalanceRequest();
            holdBalanceRequest.setMoney(buyPrice.add(transactionFee).doubleValue());
            holdBalanceRequest.setUserId(userId);
            walletService.holdBalance(internal1ServiceToken, holdBalanceRequest);

            assetService.updateAsset(internalServiceToken, adminAsset.getId(), -quantity);

            orderItem = orderItemService.createOrderItem(coinId, quantity, limitPrice.doubleValue(), 0);
        }

        Orders order = orderRedisService.createOrder(userId, orderItem, orderType);
        order.setStopPrice(stopPrice);
        order.setLimitPrice(limitPrice);
        order.setStatus(OrderStatus.PENDING);
        if(orderType == OrderType.STOP_LIMIT_BUY) {
            order.setPrice(buyPrice.add(transactionFee));
        } else {
            order.setPrice(buyPrice.subtract(transactionFee));
        }
        orderItem.setOrder(order);
        return orderRepository.save(order);
    }


    @Transactional
    private Orders placeLimitOrder(String coinId, double quantity, Long userId, BigDecimal limitPrice, OrderType orderType, String jwt) throws Exception {
        if (quantity <= 0 || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Quantity and limit price must be greater than 0");
        }

        CoinDTO coinDTO = coinService.getCoinById(coinId);
        BigDecimal buyPrice = limitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal transactionFee = buyPrice.multiply(coinDTO.getTransactionFee());
        OrderItem orderItem = null;
        if(orderType == OrderType.LIMIT_SELL) {
            AssetDTO currentAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, userId);
            if (currentAsset == null || BigDecimal.valueOf(currentAsset.getQuantity()).compareTo(BigDecimal.valueOf(quantity)) < 0) {
                throw new Exception("Insufficient assets to sell");
            }
            AssetDTO updatedAsset = assetService.updateAsset(internalServiceToken, currentAsset.getId(), -quantity);
            if (updatedAsset.getQuantity() <= 0) {
                assetService.deleteAsset(internalServiceToken, updatedAsset.getId());
            }
            orderItem = orderItemService.createOrderItem(coinId, quantity, updatedAsset.getBuyPrice(), limitPrice.doubleValue());
        } else if (orderType == OrderType.LIMIT_BUY) {
            UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
            AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, admin.getId());

            double maxBuyQuantity = adminAsset.getQuantity() * 0.1;
            if(quantity > maxBuyQuantity) {
                throw new Exception("You can only buy up to " + maxBuyQuantity + " coins per transaction.");
            }

            WalletDTO walletDTO = walletService.getUserWallet(jwt);
            if(walletDTO.getBalance().compareTo(buyPrice.add(transactionFee)) < 0) {
                throw new Exception("Insufficient balance to buy");
            }

            HoldBalanceRequest holdBalanceRequest = new HoldBalanceRequest();
            holdBalanceRequest.setMoney(buyPrice.add(transactionFee).doubleValue());
            holdBalanceRequest.setUserId(userId);
            walletService.holdBalance(internal1ServiceToken, holdBalanceRequest);

            assetService.updateAsset(internalServiceToken, adminAsset.getId(), -quantity);

            orderItem = orderItemService.createOrderItem(coinId, quantity, limitPrice.doubleValue(), 0);
        }
        Orders order = orderRedisService.createOrder(userId, orderItem, orderType);
        order.setLimitPrice(limitPrice);
        order.setStatus(OrderStatus.PENDING);
        if(orderType == OrderType.LIMIT_BUY) {
            order.setPrice(buyPrice.add(transactionFee));
        } else {
            order.setPrice(buyPrice.subtract(transactionFee));
        }
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

        UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
        double buyPrice = limitOrder.getLimitPrice().doubleValue();
        String coinId = limitOrder.getOrderItem().getCoinId();
        BigDecimal transactionFee = limitOrder.getPrice().subtract(BigDecimal.valueOf(buyPrice * quantity));

        limitOrder.setStatus(OrderStatus.SUCCESS);

        AddBalanceRequest addBalanceCustomerRequest = new AddBalanceRequest();
        addBalanceCustomerRequest.setUserId(limitOrder.getUserId());
        addBalanceCustomerRequest.setMoney(limitOrder.getPrice().doubleValue());
        addBalanceCustomerRequest.setTransactionType(WalletTransactionType.BUY_ASSET);

        walletService.commitHeldBalance(internal1ServiceToken, addBalanceCustomerRequest);

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(admin.getId());
        addBalanceRequest.setMoney(transactionFee.doubleValue());
        addBalanceRequest.setTransactionType(WalletTransactionType.CUSTOMER_BUY_ASSET);
        walletService.addBalance(internal1ServiceToken, addBalanceRequest);

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
        BigDecimal transactionFee = sellPrice.multiply(BigDecimal.valueOf(limitOrder.getOrderItem().getQuantity())).subtract(limitOrder.getPrice());

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(limitOrder.getUserId());
        addBalanceRequest.setMoney(limitOrder.getPrice().doubleValue());
        addBalanceRequest.setTransactionType(WalletTransactionType.SELL_ASSET);
        walletService.addBalance(jwt, addBalanceRequest);

        UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
        AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, limitOrder.getOrderItem().getCoinId(), admin.getId());
        assetService.updateAsset(internalServiceToken, adminAsset.getId(), limitOrder.getOrderItem().getQuantity());

        AddBalanceRequest addBalanceRequest1 = new AddBalanceRequest();
        addBalanceRequest1.setUserId(admin.getId());
        addBalanceRequest1.setMoney(transactionFee.doubleValue());
        addBalanceRequest1.setTransactionType(WalletTransactionType.CUSTOMER_SELL_ASSET);
        walletService.addBalance(internal1ServiceToken, addBalanceRequest1);

        orderRepository.save(limitOrder);
    }

    @Transactional
    private Orders buyAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }

        UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
        AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, admin.getId());

        double maxBuyQuantity = adminAsset.getQuantity() * 0.1;
        if(quantity > maxBuyQuantity) {
            throw new Exception("You can only buy up to " + maxBuyQuantity + " coins per transaction.");
        }

        CoinDTO coinDTO = coinService.getCoinById(coinId);
        double buyPrice = coinDTO.getCurrentPrice();

        BigDecimal totalPrice = BigDecimal.valueOf(buyPrice).multiply(BigDecimal.valueOf(quantity));
        BigDecimal transactionFee = coinDTO.getTransactionFee().multiply(totalPrice);

        WalletDTO userWallet = walletService.getUserWallet(jwt);
        if (userWallet.getBalance().compareTo(totalPrice.add(transactionFee)) < 0) {
            throw new Exception("Not enough money for this transaction.");
        }

        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, 0);
        Orders order = orderRedisService.createOrder(userId, orderItem, OrderType.BUY);
        orderItem.setOrder(order);
        walletService.payOrderPayment(jwt, order.getId());
        orderRedisService.updateOrderStatus(order, OrderStatus.SUCCESS);
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

        assetService.updateAsset(internalServiceToken, adminAsset.getId(), -quantity);

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(admin.getId());
        addBalanceRequest.setMoney(transactionFee.doubleValue());
        addBalanceRequest.setTransactionType(WalletTransactionType.CUSTOMER_BUY_ASSET);
        walletService.addBalance(internal1ServiceToken, addBalanceRequest);

        return orderRepository.save(order);
    }

    @Transactional
    private Orders sellAsset(String coinId, double quantity, Long userId, String jwt) throws Exception {
        if(quantity <= 0) {
            throw new Exception("quantity must be > 0");
        }

        CoinDTO coinDTO = coinService.getCoinById(coinId);
        double sellPrice = coinDTO.getCurrentPrice();
        AssetDTO assetToSell = assetService.getAssetByUserIdAndCoinId(jwt, coinId);

        if (assetToSell == null) {
            throw new Exception("Asset not found");
        }

        if (assetToSell.getQuantity() < quantity) {
            throw new Exception("Insufficient quantity to sell");
        }

        double buyPrice = assetToSell.getBuyPrice();
        BigDecimal transactionFee = coinDTO.getTransactionFee().multiply(BigDecimal.valueOf(sellPrice * quantity));
        OrderItem orderItem = orderItemService.createOrderItem(coinId, quantity, buyPrice, sellPrice);
        Orders order = orderRedisService.createOrder(userId, orderItem, OrderType.SELL);
        orderItem.setOrder(order);

        orderRedisService.updateOrderStatus(order, OrderStatus.SUCCESS);
        order.setOrderType(OrderType.SELL);
        Orders saveOrder = orderRepository.save(order);

        walletService.payOrderPayment(jwt, order.getId());
        AssetDTO updateAsset = assetService.updateAsset(internalServiceToken, assetToSell.getId(), -quantity);

        if (updateAsset.getQuantity() * sellPrice <= 0) {
            assetService.deleteAsset(internalServiceToken, updateAsset.getId());
        }
        UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
        AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coinId, admin.getId());
        assetService.updateAsset(internalServiceToken, adminAsset.getId(), quantity);

        AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
        addBalanceRequest.setUserId(admin.getId());
        addBalanceRequest.setMoney(transactionFee.doubleValue());
        addBalanceRequest.setTransactionType(WalletTransactionType.CUSTOMER_SELL_ASSET);
        walletService.addBalance(internal1ServiceToken, addBalanceRequest);

        return saveOrder;
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

        if((order.getOrderType() == OrderType.LIMIT_BUY) || (order.getOrderType() == OrderType.STOP_LIMIT_BUY)) {
            UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);
            AssetDTO adminAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, order.getOrderItem().getCoinId(), admin.getId());
            assetService.updateAsset(internalServiceToken, adminAsset.getId(), order.getOrderItem().getQuantity());

            HoldBalanceRequest holdBalanceRequest = new HoldBalanceRequest();
            holdBalanceRequest.setUserId(userId);
            holdBalanceRequest.setMoney(order.getPrice().doubleValue());
            walletService.releaseHeldBalance(internal1ServiceToken, holdBalanceRequest);
        } else if((order.getOrderType() == OrderType.LIMIT_SELL) || (order.getOrderType() == OrderType.STOP_LIMIT_SELL)) {
            AssetDTO oldAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, order.getOrderItem().getCoinId(), userId);

            if(oldAsset == null) {
                CreateAssetRequest request = new CreateAssetRequest();
                request.setUserId(userId);
                request.setCoinId(order.getOrderItem().getCoinId());
                request.setQuantity(order.getOrderItem().getQuantity());
                assetService.createAsset(internalServiceToken, request);
            } else {
                assetService.updateAsset(internalServiceToken, oldAsset.getId(), order.getOrderItem().getQuantity());
            }
        }

        orderRedisService.updateOrderStatus(order, OrderStatus.CANCELLED);
    }
}
