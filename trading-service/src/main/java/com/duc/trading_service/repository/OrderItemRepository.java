package com.duc.trading_service.repository;

import com.duc.trading_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.coinId, SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE (:startDate IS NULL OR :endDate IS NULL OR o.timestamp BETWEEN :startDate AND :endDate) " +
            "AND o.status = com.duc.trading_service.model.OrderStatus.SUCCESS " +
            "GROUP BY oi.coinId")
    List<Object[]> getTotalTransactionsByCoinInDateRange(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
