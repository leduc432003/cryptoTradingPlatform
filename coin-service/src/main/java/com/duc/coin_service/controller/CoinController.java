package com.duc.coin_service.controller;

import com.duc.coin_service.model.Coin;
import com.duc.coin_service.service.CoinService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coins")
public class CoinController {
    private final CoinService coinService;
    private final ObjectMapper objectMapper;

    @GetMapping
    ResponseEntity<List<Coin>> getCoinList(@RequestParam("page") int page) throws Exception {
        List<Coin> coinList = coinService.getCoinList(page);
        return new ResponseEntity<>(coinList, HttpStatus.OK);
    }

    @GetMapping("/{coinId}/chart")
    ResponseEntity<JsonNode> getCoinHistoricalChartData(@PathVariable String coinId, @RequestParam("days") int days) throws Exception {
        String coin = coinService.getMarketChart(coinId, days);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/{coinId}/chart/range")
    ResponseEntity<JsonNode> getCoinHistoricalChartDataRange(@PathVariable String coinId, @RequestParam("from") long from, @RequestParam("to") long to) throws Exception {
        String coin = coinService.getMarketChartRange(coinId, from, to);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/{coinId}/ohlc")
    ResponseEntity<JsonNode> getCoinOHLCChar(@PathVariable String coinId, @RequestParam("days") int days) throws Exception {
        String coin = coinService.getOHLCChar(coinId, days);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/top50")
    ResponseEntity<JsonNode> getTop50CoinByMarketCapRank() throws Exception {
        String coin = coinService.getTop50CoinsByMarketCapRank();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/search")
    ResponseEntity<JsonNode> searchCoin(@RequestParam("keyword") String keyword) throws Exception {
        String coin = coinService.searchCoin(keyword);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/details/{coinId}")
    ResponseEntity<JsonNode> getCoinDetails(@PathVariable String coinId) throws Exception {
        String coin = coinService.getCoinDetails(coinId);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/{coinId}")
    ResponseEntity<Coin> getCoinById(@PathVariable String coinId) throws Exception {
        Coin coin = coinService.findById(coinId);
        return new ResponseEntity<>(coin, HttpStatus.OK);
    }

    @GetMapping("/trending")
    ResponseEntity<JsonNode> getTrendingCoin() throws Exception {
        String coin = coinService.getTrendingCoins();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/global")
    ResponseEntity<JsonNode> getGlobal() throws Exception {
        String coin = coinService.getGlobal();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/categories")
    ResponseEntity<JsonNode> getCategoriesList(@RequestParam(required = false) String order) throws Exception {
        if(order == null) {
            order = "";
        }
        String coin = coinService.getCategoriesList(order);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/news")
    ResponseEntity<JsonNode> getNews(@RequestParam(required = false) String categories, @RequestParam(required = false) Long timestamp) throws Exception {
        if(categories == null) {
            categories = "";
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        String coin = coinService.getNews(categories, timestamp);
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }

    @GetMapping("/news-article-categories")
    ResponseEntity<JsonNode> getNewsArticleCategories() throws Exception {
        String coin = coinService.getNewsArticleCategories();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }
}
