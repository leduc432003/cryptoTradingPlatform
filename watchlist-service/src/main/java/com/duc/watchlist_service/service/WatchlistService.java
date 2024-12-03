package com.duc.watchlist_service.service;

import com.duc.watchlist_service.model.Watchlist;

import java.util.List;

public interface WatchlistService {
    Watchlist createWatchlist(Long userId, String name);
    List<Watchlist> findAllUserWatchlists(Long userId);
    Watchlist findById(Long id) throws Exception;
    Watchlist addItemToWatchList(String coinId, Long watchlistId) throws Exception;
    void deleteItemToWatchlist(String coinId, Long watchlistId) throws Exception;
    void deleteWatchlist(Long watchlistId);
}
