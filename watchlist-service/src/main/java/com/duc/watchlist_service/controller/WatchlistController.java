package com.duc.watchlist_service.controller;

import com.duc.watchlist_service.dto.CoinDTO;
import com.duc.watchlist_service.dto.UserDTO;
import com.duc.watchlist_service.dto.WatchlistResponse;
import com.duc.watchlist_service.model.Watchlist;
import com.duc.watchlist_service.service.CoinService;
import com.duc.watchlist_service.service.UserService;
import com.duc.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;
    private final UserService userService;
    private final CoinService coinService;

    @GetMapping
    public ResponseEntity<WatchlistResponse> getUserWatchlist(@RequestHeader("Authorization") String jwt) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findUserWatchList(user.getId());
        List<CoinDTO> coinDTOList = new ArrayList<>();
        if (watchlist.getCoinIds() != null && !watchlist.getCoinIds().isEmpty()) {
            coinDTOList = coinService.getCoinListByCoinIds(new ArrayList<>(watchlist.getCoinIds()));
        }

        WatchlistResponse response = WatchlistResponse.builder()
                .watchlist(watchlist)
                .listCoin(coinDTOList)
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlistById(@RequestHeader("Authorization") String jwt, @PathVariable Long watchlistId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.findById(watchlistId);
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PatchMapping("/add/coin/{coinId}")
    public ResponseEntity<Watchlist> addItemToWatchList(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.addItemToWatchList(coinId, user.getId());
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }

    @PatchMapping("/delete/coin/{coinId}")
    public ResponseEntity<Watchlist> deleteItemToWatchlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId) throws Exception {
        UserDTO user = userService.getUserProfile(jwt);
        Watchlist watchlist = watchlistService.deleteItemFromWatchList(coinId, user.getId());
        return new ResponseEntity<>(watchlist, HttpStatus.OK);
    }
}
