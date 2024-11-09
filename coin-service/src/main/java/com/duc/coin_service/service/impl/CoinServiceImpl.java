package com.duc.coin_service.service.impl;

import com.duc.coin_service.model.Coin;
import com.duc.coin_service.repository.CoinRepository;
import com.duc.coin_service.service.CoinService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    private final ObjectMapper objectMapper;
    private final CoinRepository coinRepository;
    private static final String API_KEY = "CG-HfJNVa7kfaaTEWbWDmjDQnWM";

    public Date parseIsoDate(String dateString) {
        try {
            Instant instant = Instant.parse(dateString);
            return java.util.Date.from(instant);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + dateString);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Coin> getCoinList(int page) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&price_change_percentage=1h%2C7d&page=" + page;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            List<Coin> coinList = objectMapper.readValue(response.body(), new TypeReference<List<Coin>>() {});
            return coinList;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getMarketChart(String coinId, int days) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=usd&days=" + days;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getOHLCChar(String coinId, int days) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/ohlc?vs_currency=usd&days=" + days;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getCoinDetails(String coinId) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());
            JsonNode marketData = jsonNode.get("market_data");
            Coin coin = Coin.builder()
                    .id(jsonNode.get("id").asText())
                    .name(jsonNode.get("name").asText())
                    .symbol(jsonNode.get("symbol").asText())
                    .image(jsonNode.get("image").get("large").asText())
                    .currentPrice(marketData.get("current_price").get("usd").asDouble())
                    .marketCap(marketData.get("market_cap").get("usd").asLong())
                    .marketCapRank(marketData.get("market_cap_rank").asInt())
                    .fullyDilutedValuation(marketData.get("fully_diluted_valuation").asLong())
                    .totalVolume(marketData.get("total_volume").get("usd").asLong())
                    .high24h(marketData.get("high_24h").get("usd").asDouble())
                    .low24h(marketData.get("low_24h").get("usd").asDouble())
                    .priceChange24h(marketData.get("price_change_24h").asDouble())
                    .priceChangePercentage24h(marketData.get("price_change_percentage_24h").asDouble())
                    .marketCapChange24h(marketData.get("market_cap_change_24h").asLong())
                    .marketCapChangePercentage24h((marketData.get("market_cap_change_percentage_24h").asDouble()))
                    .priceChangePercentage1hInCurrency(marketData.get("price_change_percentage_1h_in_currency").get("usd").asDouble())
                    .priceChangePercentage7dInCurrency(marketData.get("price_change_percentage_7d_in_currency").get("usd").asDouble())
                    .maxSupply(marketData.get("max_supply").asLong())
                    .totalSupply(marketData.get("total_supply").asLong())
                    .ath(marketData.get("ath").get("usd").asLong())
                    .athChangePercentage(marketData.get("ath_change_percentage").get("usd").asDouble())
                    .atl(marketData.get("atl").get("usd").asDouble())
                    .atlChangePercentage(marketData.get("atl_change_percentage").get("usd").asDouble())
                    .build();
            JsonNode athDateNode = marketData.get("ath_date");
            if (athDateNode != null && athDateNode.has("usd")) {
                String athDateUsd = athDateNode.get("usd").asText();
                if (athDateUsd != null) {
                    Date athDate = parseIsoDate(athDateUsd);
                    coin.setAthDate(athDate);
                } else {
                    System.err.println("ath_date for USD is missing");
                    coin.setAthDate(null);
                }
            } else {
                System.err.println("ath_date data is missing");
                coin.setAthDate(null);
            }

            JsonNode atlDateNode = marketData.get("atl_date");
            if (atlDateNode != null && atlDateNode.has("usd")) {
                String atlDateUsd = atlDateNode.get("usd").asText();
                if (atlDateUsd != null) {
                    Date atlDate = parseIsoDate(atlDateUsd);
                    coin.setAtlDate(atlDate);
                } else {
                    System.err.println("atl_date for USD is missing");
                    coin.setAtlDate(null);
                }
            } else {
                System.err.println("atl_date data is missing");
                coin.setAtlDate(null);
            }

            String lastUpdated = jsonNode.get("last_updated").asText();
            if (lastUpdated != null) {
                Date lastUpdatedDate = parseIsoDate(lastUpdated);
                coin.setLastUpdated(lastUpdatedDate);
            } else {
                System.err.println("Last updated date is missing");
                coin.setLastUpdated(null);
            }


            coinRepository.save(coin);
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Coin findById(String coinId) throws Exception {
        Optional<Coin> coin = coinRepository.findById(coinId);
        if(coin.isEmpty()) {
            throw new Exception("coin not found");
        }
        return coin.get();
    }

    @Override
    public String searchCoin(String keyword) throws Exception {
        String url = "https://api.coingecko.com/api/v3/search?query=" + keyword;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getTop50CoinsByMarketCapRank() throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&page=1";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getTrendingCoins() throws Exception {
        String url = "https://api.coingecko.com/api/v3/search/trending";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getGlobal() throws Exception {
        String url = "https://api.coingecko.com/api/v3/global";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getCategoriesList(String order) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/categories?order=" + order;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }
}
