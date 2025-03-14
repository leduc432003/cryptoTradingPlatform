package com.duc.wallet_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Wallet wallet;
    private WalletTransactionType walletTransactionType;
    private LocalDate date;
    private String transferId;
    private String purpose;
    @Column(precision = 19, scale = 6)
    private BigDecimal amount;
}
