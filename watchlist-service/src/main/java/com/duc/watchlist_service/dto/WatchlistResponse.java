package com.duc.watchlist_service.dto;

import com.duc.watchlist_service.model.Watchlist;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class WatchlistResponse {
    private Watchlist watchlist;
    private List<CoinDTO> listCoin;
}
