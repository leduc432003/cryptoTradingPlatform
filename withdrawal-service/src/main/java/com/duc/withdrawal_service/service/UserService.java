package com.duc.withdrawal_service.service;

import com.duc.withdrawal_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:5002")
public interface UserService {
    @GetMapping("/api/users/profile")
    UserDTO getUserProfile(@RequestHeader("Authorization") String jwt);
}
