package com.duc.trading_service.repository;

import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<Orders> findByStatusAndTradingSymbol(OrderStatus status, String tradingSymbol);
    Page<Orders> findAll(Pageable pageable);
}
