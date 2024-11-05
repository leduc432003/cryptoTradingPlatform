package com.duc.trading_service.service;

import com.duc.trading_service.dto.CoinDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coin-service", url = "http://localhost:5003")
public interface CoinService {
    @GetMapping("/api/coins/{coinId}")
    CoinDTO getCoinById(@PathVariable String coinId);
}
