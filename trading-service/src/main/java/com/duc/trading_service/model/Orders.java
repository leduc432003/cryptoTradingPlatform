package com.duc.trading_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Orders implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    @Column(nullable = false)
    private OrderType orderType;
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal price;
    @Column(precision = 19, scale = 6)
    private BigDecimal limitPrice;
    @Column(precision = 19, scale = 6)
    private BigDecimal stopPrice;
    private LocalDateTime timestamp = LocalDateTime.now();
    @Column(nullable = false)
    private OrderStatus status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderItem orderItem;
    private String tradingSymbol;

    @Override
    public String toString() {
        return "Orders{id=" + id + ", userId=" + userId + ", orderType=" + orderType + ", price=" + price + ", status=" + status + "}";
    }
}
