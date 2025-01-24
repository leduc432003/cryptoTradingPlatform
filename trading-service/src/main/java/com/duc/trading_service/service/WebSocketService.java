package com.duc.trading_service.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketService {

    private final Map<String, CoinPriceWebSocketClient> clients = new HashMap<>();
    private final String[] supportedCoins = {"btcusdt", "ethusdt", "bnbusdt"};

    public WebSocketService() {
        // Initialize WebSocket clients for all supported coins
        initializeClients();
    }

    private void initializeClients() {
        for (String coin : supportedCoins) {
            try {
                CoinPriceWebSocketClient client = new CoinPriceWebSocketClient(coin);
                clients.put(coin, client);
                client.connect(); // Start the WebSocket connection
                System.out.println("WebSocket client for " + coin + " started.");
            } catch (Exception e) {
                System.err.println("Failed to initialize WebSocket client for " + coin + ": " + e.getMessage());
            }
        }
    }

    public void startWebSocketForCoin(String coin) {
        if (!clients.containsKey(coin)) {
            try {
                CoinPriceWebSocketClient client = new CoinPriceWebSocketClient(coin);
                clients.put(coin, client);
                client.connect(); // Start the WebSocket connection
                System.out.println("WebSocket client for " + coin + " started.");
            } catch (Exception e) {
                System.err.println("Failed to start WebSocket client for " + coin + ": " + e.getMessage());
            }
        } else {
            System.out.println("WebSocket client for " + coin + " is already running.");
        }
    }

    public void stopWebSocketForCoin(String coin) {
        CoinPriceWebSocketClient client = clients.get(coin);
        if (client != null) {
            client.close();
            clients.remove(coin);
            System.out.println("WebSocket client for " + coin + " stopped.");
        } else {
            System.out.println("WebSocket client for " + coin + " is not running.");
        }
    }

    public void stopAllWebSockets() {
        for (Map.Entry<String, CoinPriceWebSocketClient> entry : clients.entrySet()) {
            entry.getValue().close();
            System.out.println("WebSocket client for " + entry.getKey() + " stopped.");
        }
        clients.clear();
    }
}
