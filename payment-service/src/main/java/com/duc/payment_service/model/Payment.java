package com.duc.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String accountNumber;
    private String bank;
    private BigDecimal amount;
    private BigDecimal amountInVnd;
    private String content;
    private PaymentStatus status;
    private String qrLink;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
