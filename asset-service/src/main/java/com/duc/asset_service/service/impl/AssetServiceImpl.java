package com.duc.asset_service.service.impl;

import com.duc.asset_service.dto.CoinDTO;
import com.duc.asset_service.model.Asset;
import com.duc.asset_service.repository.AssetRepository;
import com.duc.asset_service.service.AssetService;
import com.duc.asset_service.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetRepository assetRepository;
    private final CoinService coinService;

    @Override
    public Asset createAsset(Long userId, String coinId, double quantity) {
        CoinDTO coin = coinService.getCoinById(coinId);
        Asset asset = Asset.builder()
                .userId(userId)
                .coinId(coinId)
                .quantity(quantity)
                .buyPrice(coin.getCurrentPrice())
                .build();
        return assetRepository.save(asset);
    }

    @Override
    public Asset getAssetById(Long assetId) throws Exception {
        return assetRepository.findById(assetId).orElseThrow(() -> new Exception("asset not found"));
    }

    @Override
    public Asset getAssetByUserIdAndId(Long userId, Long assetId) {
        return null;
    }

    @Override
    public List<Asset> getAssetsByUserId(Long userId) {
        return assetRepository.findByUserId(userId);
    }

    @Override
    public Asset updateAsset(Long assetId, double quantity) throws Exception {
        Asset oldAsset = getAssetById(assetId);
        oldAsset.setQuantity(quantity + oldAsset.getQuantity());
        return assetRepository.save(oldAsset);
    }

    @Override
    public Asset findAssetByUserIdAndCoinId(Long userId, String coinId) {
        return assetRepository.findByUserIdAndCoinId(userId, coinId);
    }

    @Override
    public void deleteAsset(Long assetId) {
        assetRepository.deleteById(assetId);
    }

    @Override
    public Asset exchangeAsset(Long userId, String fromCoinId, String toCoinId, double amount) throws Exception {
        CoinDTO fromCoin = coinService.getCoinById(fromCoinId);
        CoinDTO toCoin = coinService.getCoinById(toCoinId);
        if (fromCoin == null || toCoin == null) {
            throw new Exception("Invalid coin ID(s) provided");
        }
        Asset fromAsset = findAssetByUserIdAndCoinId(userId, fromCoinId);
        if (fromAsset == null || fromAsset.getQuantity() < amount) {
            throw new Exception("Not enough balance in " + fromCoinId);
        }
        double fromCoinPrice = fromCoin.getCurrentPrice();
        double toCoinPrice = toCoin.getCurrentPrice();
        double convertedQuantity = (amount * fromCoinPrice) / toCoinPrice;
        fromAsset.setQuantity(fromAsset.getQuantity() - amount);
        if (fromAsset.getQuantity() <= 0) {
            assetRepository.delete(fromAsset);
        } else {
            assetRepository.save(fromAsset);
        }
        Asset toAsset = findAssetByUserIdAndCoinId(userId, toCoinId);
        if (toAsset == null) {
            toAsset = Asset.builder()
                    .userId(userId)
                    .coinId(toCoinId)
                    .quantity(convertedQuantity)
                    .buyPrice(toCoinPrice)
                    .build();
        } else {
            toAsset.setQuantity(toAsset.getQuantity() + convertedQuantity);
        }

        return assetRepository.save(toAsset);
    }
}
