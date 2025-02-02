package com.duc.trading_service.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketService {

    private CoinPriceWebSocketClient webSocketClient;
    private final List<String> supportedCoins = List.of("btcusdt", "ethusdt", "bnbusdt");

    public WebSocketService() {
        try {
            webSocketClient = new CoinPriceWebSocketClient(supportedCoins);
            webSocketClient.connect(); // Kết nối WebSocket
        } catch (Exception e) {
            System.err.println("Failed to initialize WebSocket client: " + e.getMessage());
        }
    }

    public void stopWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close();
            System.out.println("WebSocket stopped.");
        }
    }
}
