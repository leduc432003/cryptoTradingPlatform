package com.duc.trading_service.repository;

import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByStatus(OrderStatus status);
    List<Orders> findByStatusAndTradingSymbol(OrderStatus status, String tradingSymbol);
}
