package com.duc.asset_service.controller;

import com.duc.asset_service.dto.UserDTO;
import com.duc.asset_service.dto.request.CreateAssetRequest;
import com.duc.asset_service.model.Asset;
import com.duc.asset_service.service.AssetService;
import com.duc.asset_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asset")
public class AssetController {
    private final AssetService assetService;
    private final UserService userService;
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

    @GetMapping
    public ResponseEntity<List<Asset>> getAssetsForUser(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Asset> assets = assetService.getAssetsByUserId(user.getId());
        return new ResponseEntity<>(assets, HttpStatus.OK);
    }
}
