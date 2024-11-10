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
        List<Watchlist> watchlist = watchlistService.findUserWatchList(user.getId());
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Watchlist> createWatchlist(@RequestHeader("Authorization") String jwt) {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist createWatchlist = watchlistService.createWatchList(user.getId());
        return new ResponseEntity<>(createWatchlist, HttpStatus.CREATED);
    }

    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlistById(@RequestHeader("Authorization") String jwt, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PatchMapping("/{watchlistId}/addCoin/{coinId}")
    public ResponseEntity<CoinDTO> addItemToWatchList(@RequestHeader("Authorization") String jwt, @PathVariable String coinId, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        String coin = watchlistService.addItemToWatchList(coinDTO.getId(), user.getId(), watchlistId);
        return new ResponseEntity<>(coinDTO, HttpStatus.OK);
    }
}
