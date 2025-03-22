package com.duc.wallet_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    @Column(precision = 19, scale = 6)
    private BigDecimal balance;
    @Column(precision = 19, scale = 6)
    private BigDecimal heldBalance = BigDecimal.ZERO;
}
