package com.duc.watchlist_service.service.impl;

import com.duc.watchlist_service.dto.CoinDTO;
import com.duc.watchlist_service.model.Watchlist;
import com.duc.watchlist_service.repository.WatchlistRepository;
import com.duc.watchlist_service.service.CoinService;
import com.duc.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final CoinService coinService;

    @Override
    public Watchlist findUserWatchList(Long userId) throws Exception {
        Watchlist watchlist = watchlistRepository.findByUserId(userId);
        if(watchlist == null) {
            createWatchList(userId);
        }
        return watchlist;
    }

    @Override
    public Watchlist createWatchList(Long userId) throws Exception {
        Watchlist watchlistExist = watchlistRepository.findByUserId(userId);
        if(watchlistExist != null) {
            throw new Exception("Watchlist is exist");
        }
        Watchlist watchlist = new Watchlist();
        watchlist.setUserId(userId);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public Watchlist findById(Long id) throws Exception {
        Optional<Watchlist> watchlistOptional = watchlistRepository.findById(id);
        if(watchlistOptional.isEmpty()) {
            throw new Exception("Watchlist not found");
        }
        return watchlistOptional.get();
    }

    @Override
    public Watchlist addItemToWatchList(String coinId, Long userId) throws Exception {
        Watchlist watchlist = findUserWatchList(userId);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        if(coinDTO == null) {
            throw new Exception("Coin not found");
        }
        watchlist.getCoinIds().add(coinId);
        return watchlistRepository.save(watchlist);
    }

    @Override
    public Watchlist deleteItemFromWatchList(String coinId, Long userId) throws Exception {
        Watchlist watchlist = findUserWatchList(userId);
        CoinDTO coinDTO = coinService.getCoinById(coinId);
        if(coinDTO == null) {
            throw new Exception("Coin not found");
        }
        watchlist.getCoinIds().remove(coinId);
        return watchlistRepository.save(watchlist);
    }
}
