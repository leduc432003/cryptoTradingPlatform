package com.duc.asset_service.service.impl;

import com.duc.asset_service.dto.CoinDTO;
import com.duc.asset_service.dto.UserDTO;
import com.duc.asset_service.model.Asset;
import com.duc.asset_service.repository.AssetRepository;
import com.duc.asset_service.service.AssetService;
import com.duc.asset_service.service.CoinService;
import com.duc.asset_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetRepository assetRepository;
    private final CoinService coinService;
    private final UserService userService;
    private static final String ADMIN_EMAIL = "admin@gmail.com";

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
        UserDTO adminUser = userService.getUserByEmail(ADMIN_EMAIL);
        Asset adminAsset = assetRepository.findByUserIdAndCoinId(adminUser.getId(), toCoinId);
        double fromCoinPrice = fromCoin.getCurrentPrice();
        double toCoinPrice = toCoin.getCurrentPrice();
        double totalConvertedQuantity = (amount * fromCoinPrice) / toCoinPrice;
        BigDecimal feePercentage = toCoin.getTransactionFee();
        double feeAmount = totalConvertedQuantity * feePercentage.doubleValue();
        double netConvertedQuantity = totalConvertedQuantity - feeAmount;

        if (adminAsset == null || adminAsset.getQuantity() < totalConvertedQuantity) {
            throw new Exception("Exchange does not have enough " + toCoinId + " to complete the transaction");
        }

        fromAsset.setQuantity(fromAsset.getQuantity() - amount);
        if (fromAsset.getQuantity() <= 0) {
            assetRepository.delete(fromAsset);
        } else {
            assetRepository.save(fromAsset);
        }

        adminAsset.setQuantity(adminAsset.getQuantity() - totalConvertedQuantity);
        if (adminAsset.getQuantity() <= 0) {
            assetRepository.delete(adminAsset);
        } else {
            assetRepository.save(adminAsset);
        }

        Asset toAsset = findAssetByUserIdAndCoinId(userId, toCoinId);
        if (toAsset == null) {
            toAsset = Asset.builder()
                    .userId(userId)
                    .coinId(toCoinId)
                    .quantity(netConvertedQuantity)
                    .buyPrice(toCoinPrice)
                    .build();
        } else {
            toAsset.setQuantity(toAsset.getQuantity() + netConvertedQuantity);
        }

        assetRepository.save(toAsset);

        Asset adminFromAsset = findAssetByUserIdAndCoinId(adminUser.getId(), fromCoinId);
        if (adminFromAsset == null) {
            adminFromAsset = Asset.builder()
                    .userId(adminUser.getId())
                    .coinId(fromCoinId)
                    .quantity(amount)
                    .buyPrice(fromCoinPrice)
                    .build();
        } else {
            adminFromAsset.setQuantity(adminFromAsset.getQuantity() + amount);
        }
        assetRepository.save(adminFromAsset);

        return toAsset;
    }

    @Override
    public List<Asset> getAssetsOfAdmin() {
        UserDTO adminUser = userService.getUserByEmail(ADMIN_EMAIL);
        return assetRepository.findByUserId(adminUser.getId());
    }
}
