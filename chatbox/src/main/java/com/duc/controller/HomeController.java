package com.duc.controller;

import com.duc.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping()
    public ResponseEntity<ApiResponse> HomeController() {
        ApiResponse response = new ApiResponse();
        response.setMessage("welcome");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
