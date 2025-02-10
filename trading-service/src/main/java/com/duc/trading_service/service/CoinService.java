package com.duc.trading_service.service;

import com.duc.trading_service.dto.CoinDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "coin-service", url = "http://localhost:5003")
public interface CoinService {
    @GetMapping("/api/coins/{coinId}")
    CoinDTO getCoinById(@PathVariable String coinId);
    @GetMapping("/api/coins/get-trading-symbol")
    List<String> getTradingSymbolsByCoinIds(@RequestParam List<String> coinIds);
}
