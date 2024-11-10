package com.duc.watchlist_service.service.impl;

import com.duc.watchlist_service.model.Watchlist;
import com.duc.watchlist_service.repository.WatchlistRepository;
import com.duc.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {
    private final WatchlistRepository watchlistRepository;

    @Override
    public Watchlist createWatchList(Long userId) {
        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(userId);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public List<Watchlist> findUserWatchList(Long userId) throws Exception {
        List<Watchlist> watchlist = watchlistRepository.findByUserId(userId);
        if(watchlist == null) {
            throw new Exception("Watchlist not found.");
        }
        return watchlist;
    }

    @Override
    public Watchlist findById(Long id) throws Exception {
        Optional<Watchlist> watchlist = watchlistRepository.findById(id);
        if(watchlist.isEmpty()) {
            throw new Exception("watchlist not found.");
        }
        return watchlist.get();
    }

    @Override
    public String addItemToWatchList(String coinId, Long userId, Long watchlistId) throws Exception {
        Watchlist watchlist = findById(watchlistId);
        watchlist.getCoinIds().add(coinId);
        watchlistRepository.save(watchlist);
        return coinId;
    }
}
