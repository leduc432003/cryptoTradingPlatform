package com.duc.wallet_service.service;

import com.duc.wallet_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:5002")
public interface UserService {
    @GetMapping("/api/users/profile")
    public UserDTO getUserProfile(@RequestHeader("Authorization") String jwt);
}
