package com.duc.wallet_service.repository;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransaction;
import com.duc.wallet_service.model.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    WalletTransaction findByWalletId(Long walletId);
    List<WalletTransaction> findAllByWalletId(Long walletId);

    @Query("SELECT w FROM WalletTransaction w WHERE w.date BETWEEN :startDate AND :endDate AND (:transactionTypes IS NULL OR w.walletTransactionType IN :transactionTypes) ORDER BY w.id DESC")
    List<WalletTransaction> findAllByDateBetweenAndTransactionTypes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("transactionTypes") List<WalletTransactionType> transactionTypes);

    @Query("SELECT w FROM WalletTransaction w WHERE w.wallet.id = :walletId " +
            "AND w.date BETWEEN :startDate AND :endDate " +
            "AND w.walletTransactionType = :transactionType " +
            "ORDER BY w.id DESC")
    List<WalletTransaction> findAllByWalletIdAndDateBetweenAndType(
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("transactionType") WalletTransactionType transactionType);

    @Query("SELECT w FROM WalletTransaction w WHERE w.wallet.id = :walletId AND w.date BETWEEN :startDate AND :endDate ORDER BY w.id DESC")
    List<WalletTransaction> findAllByWalletIdAndDateBetween(
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    long countByWallet_IdAndWalletTransactionTypeIn(Long walletId, WalletTransactionType[] walletTransactionTypes);
}
