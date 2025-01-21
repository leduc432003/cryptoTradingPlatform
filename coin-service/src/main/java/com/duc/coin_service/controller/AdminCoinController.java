package com.duc.coin_service.controller;

import com.duc.coin_service.dto.UserDTO;
import com.duc.coin_service.dto.UserRole;
import com.duc.coin_service.dto.request.UpdateCoinRequest;
import com.duc.coin_service.model.Coin;
import com.duc.coin_service.service.CoinService;
import com.duc.coin_service.dto.request.AddCoinRequest;
import com.duc.coin_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coins")
public class AdminCoinController {
    private final CoinService coinService;
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<Coin> addCoin(@RequestHeader("Authorization") String jwt, @RequestBody AddCoinRequest request) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can add coin");
        }
        Coin newCoin = coinService.addCoin(request.getCoinId(), request.getMinimumBuyPrice(), request.getTransactionFee());
        return new ResponseEntity<>(newCoin, HttpStatus.CREATED);
    }

    @PutMapping("/{coinId}")
    public ResponseEntity<Coin> updateCoin(@RequestHeader("Authorization") String jwt, @PathVariable String coinId, @RequestBody UpdateCoinRequest request) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can update coin");
        }
        Coin newCoin = coinService.updateCoin(coinId, request.getMinimumBuyPrice(), request.getTransactionFee(), request.getTotalSupply());
        return new ResponseEntity<>(newCoin, HttpStatus.OK);
    }

    @DeleteMapping("/{coinId}")
    public ResponseEntity<String> deleteCoin(@RequestHeader("Authorization") String jwt, @PathVariable String coinId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        if(user.getRole() != UserRole.ROLE_ADMIN) {
            throw new Exception("Only admin can delete coin");
        }
        coinService.deleteCoin(coinId);
        return new ResponseEntity<>("Coin deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/{coinId}/is-new")
    public Coin updateIsNewStatus(@RequestHeader("Authorization") String jwt, @PathVariable String coinId, @RequestParam boolean isNew) {
        return coinService.updateIsNewStatus(coinId, isNew);
    }
}
