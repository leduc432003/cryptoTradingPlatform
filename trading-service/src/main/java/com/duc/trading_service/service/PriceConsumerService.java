package com.duc.trading_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PriceConsumerService {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "coin-prices", groupId = "trading-service-group")
    public void consumePriceData(String message) {
        try {
            Map<String, String> priceData = objectMapper.readValue(message, Map.class);
            String symbol = priceData.get("symbol");
            BigDecimal currentPrice = new BigDecimal(priceData.get("price"));

            // Gọi hàm khớp lệnh
            orderService.matchOrdersWithPrice(symbol, currentPrice);
        } catch (Exception e) {
            System.err.println("Error processing Kafka message: " + e.getMessage());
        }
    }
}