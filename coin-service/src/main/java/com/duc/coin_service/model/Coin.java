package com.duc.coin_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coin {
    @Id
    @JsonProperty("id")
    private String id;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("name")
    private String name;
    @JsonProperty("image")
    private String image;
    @JsonProperty("current_price")
    private double currentPrice;
    @JsonProperty("market_cap")
    private long marketCap;
    @JsonProperty("market_cap_rank")
    private int marketCapRank;
    @JsonProperty("circulating_supply")
    private long circulatingSupply;
    @JsonProperty("fully_diluted_valuation")
    private long fullyDilutedValuation;
    @JsonProperty("total_volume")
    private long totalVolume;
    @JsonProperty("high_24h")
    private double high24h;
    @JsonProperty("low_24h")
    private double low24h;
    @JsonProperty("price_change_24h")
    private double priceChange24h;
    @JsonProperty("price_change_percentage_24h")
    private double priceChangePercentage24h;
    @JsonProperty("market_cap_change_24h")
    private long marketCapChange24h;
    @JsonProperty("market_cap_change_percentage_24h")
    private double marketCapChangePercentage24h;
    @JsonProperty("total_supply")
    private long totalSupply;
    @JsonProperty("max_supply")
    private long maxSupply;
    @JsonProperty("ath")
    private double ath;
    @JsonProperty("ath_change_percentage")
    private double athChangePercentage;
    @JsonProperty("ath_date")
    private Date athDate;
    @JsonProperty("atl")
    private double atl;
    @JsonProperty("atl_change_percentage")
    private double atlChangePercentage;
    @JsonProperty("atl_date")
    private Date atlDate;
    @JsonProperty("roi")
    @JsonIgnore
    private String roi;
    @JsonProperty("last_updated")
    private Date lastUpdated;
    @JsonProperty("price_change_percentage_1h_in_currency")
    private double priceChangePercentage1hInCurrency;
    @JsonProperty("price_change_percentage_7d_in_currency")
    private double priceChangePercentage7dInCurrency;
    @JsonProperty("minimum_buy_price")
    @Column(precision = 19, scale = 6)
    private BigDecimal minimumBuyPrice;
    @JsonProperty("transaction_fee")
    @Column(precision = 19, scale = 6)
    private BigDecimal transactionFee;
    @JsonProperty("is_new")
    private boolean isNew = false;
    @JsonProperty("is_delisted")
    private boolean isDelisted = false;
    @JsonProperty("trading_symbol")
    private String tradingSymbol;
}
