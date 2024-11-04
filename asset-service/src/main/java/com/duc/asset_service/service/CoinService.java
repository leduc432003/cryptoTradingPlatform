package com.duc.asset_service.service;

import com.duc.asset_service.dto.CoinDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coin-service", url = "http://localhost:5003")
public interface CoinService {
    @GetMapping("/api/coins/{coinId}")
    public CoinDTO getCoinById(@PathVariable String coinId);
}
