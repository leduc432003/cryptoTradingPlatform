package com.duc.watchlist_service.service.impl;

import com.duc.watchlist_service.dto.CoinDTO;
import com.duc.watchlist_service.model.Watchlist;
import com.duc.watchlist_service.repository.WatchlistRepository;
import com.duc.watchlist_service.service.CoinService;
import com.duc.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final CoinService coinService;

    @Override
    public Watchlist createWatchlist(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Watchlist name is required.");
        }
        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(userId);
        watchlist.setName(name);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public List<Watchlist> findAllUserWatchlists(Long userId) {
        return watchlistRepository.findByUserId(userId);
    }

    @Override
    public Watchlist findById(Long id) throws Exception {
        return watchlistRepository.findById(id)
                .orElseThrow(() -> new Exception("Watchlist not found."));
    }

    @Override
    public Watchlist addItemToWatchList(String coinId, Long watchlistId) throws Exception {
        Watchlist watchlist = findById(watchlistId);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        if(coinDTO == null) {
            throw new Exception("Coin not found");
        }
        watchlist.getCoinIds().add(coinId);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public void deleteItemToWatchlist(String coinId, Long watchlistId) throws Exception {
        Watchlist watchlist = findById(watchlistId);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        if(coinDTO == null) {
            throw new Exception("Coin not found");
        }
        watchlist.getCoinIds().remove(coinId);
        watchlistRepository.save(watchlist);
    }

    @Override
    public void deleteWatchlist(Long watchlistId) {
        watchlistRepository.deleteById(watchlistId);
    }
}
