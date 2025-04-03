package com.duc.trading_service.config;

import com.duc.trading_service.service.CoinPriceWebSocketClient;
import com.duc.trading_service.service.CoinService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebSocketConfig {

    private final CoinService coinService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public WebSocketConfig(CoinService coinService, KafkaTemplate<String, String> kafkaTemplate) {
        this.coinService = coinService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public CoinPriceWebSocketClient coinPriceWebSocketClient() throws Exception {
        Set<String> pendingCoins = new HashSet<>(coinService.getTradingSymbols());
        return new CoinPriceWebSocketClient(pendingCoins, kafkaTemplate);
    }
}