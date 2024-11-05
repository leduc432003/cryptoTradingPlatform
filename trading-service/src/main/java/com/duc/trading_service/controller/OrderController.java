package com.duc.trading_service.controller;

import com.duc.trading_service.dto.UserDTO;
import com.duc.trading_service.dto.request.CreateOrderRequest;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.service.OrderService;
import com.duc.trading_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Orders> payOrderPayment(@RequestHeader("Authorization") String jwt, @RequestBody CreateOrderRequest request) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Orders order = orderService.processOrder(request.getCoinId(), request.getQuantity(), request.getOrderType(), user.getId());
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Orders> getOrderById(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Orders order = orderService.getOrderById(orderId);
        if(order.getUserId().equals(user.getId())) {
            return new ResponseEntity<>(order, HttpStatus.OK);
        }
        throw new Exception("you don't have a role to access");
    }

    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrdersForUser(@RequestHeader("Authorization") String jwt, @RequestParam(required = false) OrderType order_type, @RequestParam(required = false) String asset_symbol) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Orders> orderList = orderService.getAllOrdersOfUser(user.getId(), order_type, asset_symbol);
        return new ResponseEntity<>(orderList, HttpStatus.OK);
    }
}
