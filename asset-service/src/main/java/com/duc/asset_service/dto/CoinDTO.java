package com.duc.asset_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CoinDTO {
    private String id;
    private String symbol;
    private String name;
    private String image;
    private double currentPrice;
    private long marketCap;
    private int marketCapRank;
    private long fullyDilutedValuation;
    private long totalVolume;
    private double high24h;
    private double low24h;
    private double priceChange24h;
    private double priceChangePercentage24h;
    private long marketCapChange24h;
    private double marketCapChangePercentage24h;
    private long totalSupply;
    private long maxSupply;
    private double ath;
    private double athChangePercentage;
    private Date athDate;
    private double atl;
    private double atlChangePercentage;
    private Date atlDate;
    @JsonIgnore
    private String roi;
    private Date lastUpdated;
}
