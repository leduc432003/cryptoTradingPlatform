package com.duc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CoinDto {
    private String id;
    private String name;
    private String symbol;
    private String image;
    private double currentPrice;
    private double marketCap;
    private double marketCapRank;
    private double totalVolume;
    private double high24h;
    private double low24h;
    private double priceChange24h;
    private double priceChangePercentage24h;
    private double marketCapChange24h;
    private double marketCapChangePercentage24;
    private double circulatingSupply;
    private double totalSupply;
    private double ath;
    private double athChangePercentage;
    private Date athDate;
    private double atl;
    private double atlChangePercentage;
    private Date atlDate;
    private Date lastUpdated;
    private double priceChangePercentage1hInCurrency;
    private double priceChangePercentage7dInCurrency;
}
