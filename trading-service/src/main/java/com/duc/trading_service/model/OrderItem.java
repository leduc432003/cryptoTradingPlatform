package com.duc.trading_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double quantity;
    private String coinId;
    private double buyPrice;
    private double sellPrice;

    @JsonIgnore
    @OneToOne
    private Orders order;

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", quantity=" + quantity + ", coinId='" + coinId + "', buyPrice=" + buyPrice + "}";
    }
}
