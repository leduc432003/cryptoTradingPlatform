package com.duc.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "coin-service", url = "http://localhost:5003")
public interface CoinService {
    @GetMapping("/api/coins/get-coin-id")
    String getCoinId(@RequestParam String name);
}
