package com.duc.coin_service.repository;

import com.duc.coin_service.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {
    @Query("SELECT c.id FROM Coin c")
    List<String> findAllCoinIds();
    List<Coin> findByIsNewTrue();
    List<Coin> findByIsDelistedTrue();
    @Query("SELECT c.tradingSymbol FROM Coin c")
    List<String> findAllTradingSymbols();
}