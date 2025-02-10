package com.duc.trading_service.service;

import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private CoinPriceWebSocketClient webSocketClient;
    private final OrderService orderService;

    @PostConstruct
    public void startWebSocket() {
        try {
            List<String> pendingCoins = orderService.getPendingCoinSymbols();
            if (!pendingCoins.isEmpty()) {
                webSocketClient = new CoinPriceWebSocketClient(pendingCoins);
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

//    public void addNewCoin(String newCoinSymbol) {
//        if (webSocketClient != null) {
//            webSocketClient.subscribeNewCoin(newCoinSymbol);
//        }
//    }
}
