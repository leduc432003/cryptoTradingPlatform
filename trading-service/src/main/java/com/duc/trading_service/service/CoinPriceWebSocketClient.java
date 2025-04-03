package com.duc.trading_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Component
public class CoinPriceWebSocketClient extends WebSocketClient {
    private final Set<String> coinSymbols;
    private final Map<String, String> coinData = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate; // Injected via constructor

    public CoinPriceWebSocketClient(Set<String> coinSymbols, KafkaTemplate<String, String> kafkaTemplate) throws Exception {
        super(new URI("wss://stream.binance.com:9443/ws"));
        this.coinSymbols = coinSymbols;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WebSocket Connected");
        sendSubscribeMessage();
    }

    private void sendSubscribeMessage() {
        try {
            Map<String, Object> subscribePayload = new HashMap<>();
            subscribePayload.put("method", "SUBSCRIBE");
            subscribePayload.put("params", coinSymbols.stream()
                    .map(coin -> coin.toLowerCase() + "@kline_15m")
                    .toArray(String[]::new));
            subscribePayload.put("id", coinSymbols.size());

            String subscribeMessage = objectMapper.writeValueAsString(subscribePayload);
            this.send(subscribeMessage);
            System.out.println("Sent SUBSCRIBE request: " + subscribeMessage);
        } catch (Exception e) {
            System.err.println("Error creating subscribe message: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        handlePriceData(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket Closed. Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void handlePriceData(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("e") && "kline".equals(jsonNode.get("e").asText())) {
                String symbol = jsonNode.get("s").asText();
                JsonNode klineData = jsonNode.get("k");
                if (klineData != null && klineData.has("c")) {
                    String closePrice = klineData.get("c").asText();
                    coinData.put(symbol, closePrice);
                    System.out.println("Updated price for " + symbol + ": " + closePrice);

                    Map<String, String> priceData = new HashMap<>();
                    priceData.put("symbol", symbol.toLowerCase());
                    priceData.put("price", closePrice);
                    kafkaTemplate.send("coin-prices", symbol, objectMapper.writeValueAsString(priceData));
                } else {
                    System.err.println("Invalid kline data: " + message);
                }
            } else {
                System.err.println("Unexpected message format: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error parsing price data: " + e.getMessage());
        }
    }
}