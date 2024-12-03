package com.duc.watchlist_service.controller;

import com.duc.watchlist_service.dto.CoinDTO;
import com.duc.watchlist_service.dto.UserDTO;
import com.duc.watchlist_service.model.Watchlist;
import com.duc.watchlist_service.service.CoinService;
import com.duc.watchlist_service.service.UserService;
import com.duc.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Watchlist>> getUserWatchlists(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        List<Watchlist> watchlists = watchlistService.findAllUserWatchlists(user.getId());
        return new ResponseEntity<>(watchlists, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createWatchlist(@RequestHeader("Authorization") String jwt, @RequestParam String name) {
        UserDTO user = userService.getUserProfile(jwt);
        try {
            Watchlist watchlist = watchlistService.createWatchlist(user.getId(), name);
            return ResponseEntity.status(HttpStatus.CREATED).body(watchlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/addCoin/{coinId}/{watchlistId}")
    public ResponseEntity<Watchlist> addItemToWatchList(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId,
            @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to access this watchlist");
        }
        Watchlist updatedWatchlist = watchlistService.addItemToWatchList(coinId, watchlistId);
        return ResponseEntity.ok(updatedWatchlist);
    }

    @PatchMapping("/deleteCoin/{coinId}/{watchlistId}")
    public ResponseEntity<String> deleteItemToWatchlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId,
            @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to access this watchlist");
        }
        watchlistService.deleteItemToWatchlist(coinId, watchlistId);
        return ResponseEntity.ok("delete coin success.");
    }

        @DeleteMapping("/{watchlistId}")
    public ResponseEntity<String> deleteWatchlist(@RequestHeader("Authorization") String jwt, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to access this watchlist");
        }
        watchlistService.deleteWatchlist(watchlistId);
        return ResponseEntity.ok("Watchlist deleted successfully.");
    }
}
