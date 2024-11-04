package com.duc.asset_service.service;

import com.duc.asset_service.model.Asset;

import java.util.List;

public interface AssetService {
    Asset createAsset(Long userId, String coinId, double quantity);
    Asset getAssetById(Long assetId) throws Exception;
    Asset getAssetByUserIdAndId(Long userId, Long assetId);
    List<Asset> getAssetsByUserId(Long userId);
    Asset updateAsset(Long assetId, double quantity) throws Exception;
    Asset findAssetByUserIdAndCoinId(Long userId, String coinId);
    void deleteAsset(Long assetId);
}
