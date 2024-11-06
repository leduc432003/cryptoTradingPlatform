package com.duc.wallet_service.service;

import com.duc.wallet_service.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "trading-service", url = "http://localhost:5006")
public interface OrderService {
    @GetMapping("/api/orders/{orderId}")
    OrderDTO getOrderById(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId);
}
