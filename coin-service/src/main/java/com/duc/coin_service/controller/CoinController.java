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

    @GetMapping("/trending")
    ResponseEntity<JsonNode> getTrendingCoin() throws Exception {
        String coin = coinService.getTrendingCoins();
        JsonNode jsonNode = objectMapper.readTree(coin);
        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }
}
