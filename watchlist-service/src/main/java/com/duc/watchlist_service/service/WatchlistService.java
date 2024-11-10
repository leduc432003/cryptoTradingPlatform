package com.duc.watchlist_service.service;

import com.duc.watchlist_service.model.Watchlist;

import java.util.List;

public interface WatchlistService {
    Watchlist createWatchList(Long userId);
    List<Watchlist> findUserWatchList(Long userId) throws Exception;
    Watchlist findById(Long id) throws Exception;
    String addItemToWatchList(String coinId, Long userId, Long watchlistId) throws Exception;
}
