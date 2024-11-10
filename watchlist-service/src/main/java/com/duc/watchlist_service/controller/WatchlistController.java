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
    private final CoinService coinService;

    @GetMapping
    public ResponseEntity<List<Watchlist>> getUserWatchlist(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        List<Watchlist> watchlist = watchlistService.findUserWatchlist(user.getId());
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Watchlist> createWatchlist(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist createWatchlist = watchlistService.createWatchlist(user.getId());
        return new ResponseEntity<>(createWatchlist, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{watchlistId}")
    public ResponseEntity<String> deleteWatchlist(@RequestHeader("Authorization") String jwt, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        watchlistService.deleteWatchlist(watchlistId);
        return new ResponseEntity<>("Delete watchlist successfully.", HttpStatus.CREATED);
    }

    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlistById(@RequestHeader("Authorization") String jwt, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PatchMapping("/{watchlistId}/addCoin/{coinId}")
    public ResponseEntity<CoinDTO> addItemToWatchList(@RequestHeader("Authorization") String jwt, @PathVariable String coinId, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        String coin = watchlistService.addItemToWatchList(coinDTO.getId(), watchlistId);
        return new ResponseEntity<>(coinDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{watchlistId}/deleteCoin/{coinId}")
    public ResponseEntity<String> deleteItemToWatchList(@RequestHeader("Authorization") String jwt, @PathVariable String coinId, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        if (!watchlist.getUserId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        watchlistService.deleteItemToWatchlist(coinId, watchlistId);
        return new ResponseEntity<>("Delete coin successfully.", HttpStatus.OK);
    }
}
