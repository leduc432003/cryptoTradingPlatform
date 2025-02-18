package com.duc.asset_service.controller;

import com.duc.asset_service.dto.CoinDTO;
import com.duc.asset_service.dto.UserDTO;
import com.duc.asset_service.dto.request.CreateAssetRequest;
import com.duc.asset_service.dto.response.AssetResponse;
import com.duc.asset_service.model.Asset;
import com.duc.asset_service.service.AssetService;
import com.duc.asset_service.service.CoinService;
import com.duc.asset_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asset")
public class AssetController {
    private final AssetService assetService;
    private final UserService userService;
    private final CoinService coinService;
    @Value("${internal.service.token}")
    private String internalServiceToken;

    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestHeader("Internal-Service-Token") String jwt, @RequestBody CreateAssetRequest request) throws Exception {
        if (!internalServiceToken.equals(jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Asset asset = assetService.createAsset(request.getUserId(), request.getCoinId(), request.getQuantity());
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @PutMapping("/{assetId}")
    public ResponseEntity<Asset> updateAsset(@RequestHeader("Internal-Service-Token") String jwt, @PathVariable Long assetId, @RequestParam("quantity") double quantity) throws Exception {
        if (!internalServiceToken.equals(jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Asset asset = assetService.updateAsset(assetId, quantity);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<String> deleteAsset(@RequestHeader("Internal-Service-Token") String jwt, @PathVariable Long assetId) throws Exception {
        if (!internalServiceToken.equals(jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        assetService.deleteAsset(assetId);
        return new ResponseEntity<>("Delete asset successfully.", HttpStatus.OK);
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<Asset> getAssetById(@RequestHeader("Authorization") String jwt, @PathVariable Long assetId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Asset asset = assetService.getAssetById(assetId);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @GetMapping("/coin/{coinId}/user")
    public ResponseEntity<Asset> getAssetByUserIdAndCoinId(@RequestHeader("Authorization") String jwt, @PathVariable String coinId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Asset asset = assetService.findAssetByUserIdAndCoinId(user.getId(), coinId);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @GetMapping("/coin/{coinId}/user/{userId}")
    public ResponseEntity<Asset> getAssetByUserIdAndCoinIdInternal(@RequestHeader("Internal-Service-Token") String jwt, @PathVariable String coinId, @PathVariable Long userId) throws Exception {
        Asset asset = assetService.findAssetByUserIdAndCoinId(userId, coinId);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssetsForUser(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Asset> assets = assetService.getAssetsByUserId(user.getId());

        List<String> coinIds = assets.stream()
                .map(Asset::getCoinId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, CoinDTO> coinMap = coinService.getCoinListByCoinIds(coinIds).stream()
                .collect(Collectors.toMap(CoinDTO::getId, coin -> coin));

        List<AssetResponse> responseList = assets.stream()
                .map(asset -> AssetResponse.builder()
                        .id(asset.getId())
                        .userId(asset.getUserId())
                        .quantity(asset.getQuantity())
                        .buyPrice(asset.getBuyPrice())
                        .coinDTO(coinMap.get(asset.getCoinId()))
                        .build())
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @PostMapping("/exchange")
    public ResponseEntity<Asset> exchangeAsset(@RequestHeader("Authorization") String jwt, @RequestParam String fromCoinId, @RequestParam String toCoinId, @RequestParam double amount) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Asset updatedAsset = assetService.exchangeAsset(user.getId(), fromCoinId, toCoinId, amount);
        return new ResponseEntity<>(updatedAsset, HttpStatus.OK);
    }
}
