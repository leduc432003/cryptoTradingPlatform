package com.duc.watchlist_service.service;

import com.duc.watchlist_service.model.Watchlist;

public interface WatchlistService {
    Watchlist findUserWatchList(Long userId) throws Exception;
    Watchlist createWatchList(Long userId) throws Exception;
    Watchlist findById(Long id) throws Exception;
    Watchlist addItemToWatchList(String coinId, Long userId) throws Exception;
    void deleteItemFromWatchList(String coinId, Long userId) throws Exception;
}
