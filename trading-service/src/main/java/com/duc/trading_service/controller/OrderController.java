package com.duc.trading_service.controller;

import com.duc.trading_service.dto.UserDTO;
import com.duc.trading_service.dto.UserRole;
import com.duc.trading_service.dto.request.CreateOrderRequest;
import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.Orders;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.service.OrderItemService;
import com.duc.trading_service.service.OrderService;
import com.duc.trading_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<?> payOrderPayment(@RequestHeader("Authorization") String jwt, @RequestBody CreateOrderRequest request) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);

        Orders order = orderService.processOrder(request.getCoinId(), request.getQuantity(), BigDecimal.valueOf(request.getStopPrice()),
                BigDecimal.valueOf(request.getLimitPrice()), request.getOrderType(),
                user.getId(), jwt);
        return ResponseEntity.ok(order);
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

    @GetMapping("/pending")
    public ResponseEntity<List<Orders>> getAllOrdersForUser(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Orders> orderList = orderService.getOrdersByStatus(user.getId(), OrderStatus.PENDING);
        return new ResponseEntity<>(orderList, HttpStatus.OK);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelLimitOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Orders orders = orderService.getOrderById(orderId);
        if (!orders.getUserId().equals(user.getId())) {
            throw new Exception("You are not authorized to access this order");
        }
        try {
            orderService.cancelLimitOrder(orderId, user.getId());
            return ResponseEntity.ok("Order canceled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<Orders>> getAllOrders(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("You are not authorized to access.");
        }

        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Orders> orderPage = orderService.getAllOrders(pageable);

        return new ResponseEntity<>(orderPage, HttpStatus.OK);
    }

    @GetMapping("/admin/total-transactions-range")
    public Map<String, Double> getTotalTransactionsByCoinInDateRange(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("You are not authorized to access.");
        }

        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now();
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : start;

        return orderItemService.getTotalTransactionsByCoinInDateRange(start, end);
    }
}
