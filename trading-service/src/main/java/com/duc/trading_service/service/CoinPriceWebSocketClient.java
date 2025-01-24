package com.duc.trading_service.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import java.net.URI;

public class CoinPriceWebSocketClient extends WebSocketClient {
    private final String coinSymbol;

    public CoinPriceWebSocketClient(String coinSymbol) throws Exception {
        super(new URI("wss://stream.binance.com:9443/ws/" + coinSymbol + "@kline_15m"));
        this.coinSymbol = coinSymbol.toUpperCase();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WebSocket Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received for " + coinSymbol + ": " + message);
        handlePriceData(coinSymbol, message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket Closed. Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void handlePriceData(String coinSymbol, String message) {
        System.out.println("Processing price data for " + coinSymbol);
        // Thêm logic xử lý dữ liệu JSON tại đây
    }
}
