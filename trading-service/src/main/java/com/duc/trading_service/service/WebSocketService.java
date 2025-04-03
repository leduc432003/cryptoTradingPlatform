package com.duc.trading_service.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final CoinPriceWebSocketClient webSocketClient; // Injected by Spring
    private final CoinService coinService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    public void startWebSocket() {
        try {
            Set<String> pendingCoins = new HashSet<>(coinService.getTradingSymbols());
            if (!pendingCoins.isEmpty()) {
                // The webSocketClient is already initialized with pendingCoins via constructor injection
                webSocketClient.connect();
            } else {
                System.out.println("No pending coins to track.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing WebSocket: " + e.getMessage());
        }
    }

    public void stopWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            System.out.println("WebSocket stopped.");
        }
    }
}