package com.duc.trading_service.repository;

import com.duc.trading_service.model.OrderStatus;
import com.duc.trading_service.model.OrderType;
import com.duc.trading_service.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserId(Long userId);
    List<Orders> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<Orders> findByStatusAndTradingSymbol(OrderStatus status, String tradingSymbol);
    @Query("SELECT o FROM Orders o WHERE " +
            "(:orderType IS NULL OR o.orderType = :orderType) " +
            "AND (:assetSymbol IS NULL OR o.tradingSymbol = :assetSymbol) " +
            "AND (:startDate IS NULL OR o.timestamp >= :startDate) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Orders> findAllWithFilters(@Param("orderType") OrderType orderType,
                                    @Param("assetSymbol") String assetSymbol,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("status") OrderStatus status,
                                    Pageable pageable);
    @Query("SELECT o FROM Orders o WHERE o.userId = :userId " +
            "AND (:orderType IS NULL OR o.orderType = :orderType) " +
            "AND (:assetSymbol IS NULL OR o.tradingSymbol = :assetSymbol) " +
            "AND (:startDate IS NULL OR o.timestamp >= :startDate) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.timestamp DESC")
    List<Orders> findOrdersByUserIdAndFilters(@Param("userId") Long userId,
                                              @Param("orderType") OrderType orderType,
                                              @Param("assetSymbol") String assetSymbol,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("status") OrderStatus status);
}
