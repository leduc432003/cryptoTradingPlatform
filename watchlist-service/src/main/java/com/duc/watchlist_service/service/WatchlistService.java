package com.duc.watchlist_service.service;

import com.duc.watchlist_service.model.Watchlist;

import java.util.List;

public interface WatchlistService {
    Watchlist createWatchlist(Long userId);
    List<Watchlist> findUserWatchlist(Long userId) throws Exception;
    Watchlist findById(Long id) throws Exception;
    String addItemToWatchList(String coinId, Long watchlistId) throws Exception;
    void deleteWatchlist(Long id);
    void deleteItemToWatchlist(String coinId, Long watchlistId) throws Exception;
}
