package com.duc.wallet_service.repository;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    WalletTransaction findByWalletId(Long walletId);
    List<WalletTransaction> findAllByWalletId(Long walletId);
}
