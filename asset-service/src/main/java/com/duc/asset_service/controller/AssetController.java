package com.duc.asset_service.controller;

import com.duc.asset_service.dto.UserDTO;
import com.duc.asset_service.model.Asset;
import com.duc.asset_service.service.AssetService;
import com.duc.asset_service.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{assetId}")
    public ResponseEntity<Asset> getAssetById(@RequestHeader("Authorization") String jwt, @PathVariable Long assetId) throws Exception {
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
