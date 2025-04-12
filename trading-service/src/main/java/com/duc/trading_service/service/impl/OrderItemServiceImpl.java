package com.duc.trading_service.service.impl;

import com.duc.trading_service.model.OrderItem;
import com.duc.trading_service.repository.OrderItemRepository;
import com.duc.trading_service.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItem createOrderItem(String coinId, double quantity, double buyPrice, double sellPrice) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCoinId(coinId);
        orderItem.setQuantity(quantity);
        orderItem.setBuyPrice(buyPrice);
        orderItem.setSellPrice(sellPrice);
        return orderItemRepository.save(orderItem);
    }

    @Override
    public Map<String, Double> getTotalTransactionsByCoinInDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        }

        List<Object[]> results = orderItemRepository.getTotalTransactionsByCoinInDateRange(startDateTime, endDateTime);
        Map<String, Double> transactions = new HashMap<>();
        for (Object[] result : results) {
            transactions.put((String) result[0], (Double) result[1]);
        }
        return transactions;
    }
}
