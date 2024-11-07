package com.duc.trading_service.service;

import com.duc.trading_service.dto.AssetDTO;
import com.duc.trading_service.dto.request.CreateAssetRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "asset-service", url = "http://localhost:5005")
public interface AssetService {
    @PostMapping("/api/asset")
    AssetDTO createAsset(@RequestHeader("Internal-Service-Token") String jwt, @RequestBody CreateAssetRequest request);
    @GetMapping("/api/asset/coin/{coinId}/user")
    AssetDTO getAssetByUserIdAndCoinId(@RequestHeader("Authorization") String jwt, @PathVariable String coinId);
    @PutMapping("/api/asset/{assetId}")
    AssetDTO updateAsset(@RequestHeader("Internal-Service-Token") String jwt, @PathVariable Long assetId, @RequestParam("quantity") double quantity);
    @DeleteMapping("/api/asset/{assetId}")
    ResponseEntity<String> deleteAsset(@RequestHeader("Internal-Service-Token") String jwt, @PathVariable Long assetId);
}
