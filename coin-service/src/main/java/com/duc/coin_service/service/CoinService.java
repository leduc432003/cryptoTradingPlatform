package com.duc.coin_service.service;

import com.duc.coin_service.model.Coin;

import javax.swing.*;
import java.util.List;

public interface CoinService {
    List<Coin> getCoinList(int page) throws Exception;
    String getMarketChart(String coinId, int days) throws Exception;
    String getMarketChartRange(String coinId, long from, long to) throws Exception;
    String getOHLCChar(String coinId, int days) throws Exception;
    String getCoinDetails(String coinId) throws Exception;
    Coin findById(String coinId) throws Exception;
    String searchCoin(String keyword) throws Exception;
    String getTop50CoinsByMarketCapRank() throws Exception;
    String getTrendingCoins() throws Exception;
    String getGlobal() throws Exception;
    String getCategoriesList(String order) throws Exception;
    String getNews(String categories, Long timestamp) throws Exception;
    String getNewsArticleCategories() throws Exception;
    Coin addCoin(String coinId, double minimumBuyPrice, double transactionFee) throws Exception;
    Coin updateCoin(String coinId, double minimumBuyPrice, double transactionFee) throws Exception;
    void deleteCoin(String coinId);
    Coin updateIsNewStatus(String id, boolean isNew);
    List<Coin> getNewCoins();
}
