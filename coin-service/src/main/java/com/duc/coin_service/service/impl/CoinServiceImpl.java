package com.duc.coin_service.service.impl;

import com.duc.coin_service.dto.AssetDTO;
import com.duc.coin_service.dto.UserDTO;
import com.duc.coin_service.dto.request.CreateAssetRequest;
import com.duc.coin_service.model.Coin;
import com.duc.coin_service.repository.CoinRepository;
import com.duc.coin_service.service.AssetService;
import com.duc.coin_service.service.CoinService;
import com.duc.coin_service.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    private final ObjectMapper objectMapper;
    private final CoinRepository coinRepository;
    private final AssetService assetService;
    private final UserService userService;
    @Value("${internal.service.token}")
    private String internalServiceToken;
    private static final String API_KEY = "CG-HfJNVa7kfaaTEWbWDmjDQnWM";
    private static final String ADMIN_EMAIL = "admin@gmail.com";

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
        List<String> coinList1 = coinRepository.findAllCoinIds();
        String list = coinList1.toString().replaceAll("[\\[\\]]", "").replaceAll("\\s+", "");
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&price_change_percentage=1h%2C7d&page=" + page + "&ids=" + list;
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
    public List<Coin> getCoinListByListCoinId(List<String> coinIds) throws Exception {
        String list = String.join(",", coinIds);
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&price_change_percentage=1h%2C7d&ids=" + list;
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
    public List<Coin> getCoinListVolume(int page, Boolean volume) throws Exception {
        List<String> coinList1 = coinRepository.findAllCoinIds();
        String list = coinList1.toString().replaceAll("[\\[\\]]", "").replaceAll("\\s+", "");
        String order = "";
        if(Boolean.TRUE.equals(volume)) {
            order = "volume_asc";
        } else {
            order = "volume_desc";
        }
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&price_change_percentage=1h%2C7d&page=" + page + "&ids=" + list + "&order=" + order;
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
    public String getMarketChartRange(String coinId, long from, long to) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart/range?vs_currency=usd&from=" + from + "&to=" + to;
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
                    .fullyDilutedValuation(marketData.get("fully_diluted_valuation").get("usd").asLong())
                    .totalVolume(marketData.get("total_volume").get("usd").asLong())
                    .circulatingSupply(marketData.get("circulating_supply").asLong())
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


            if (coinRepository.existsById(coinId)) {
                Optional<Coin> coin1 = coinRepository.findById(coinId);
                coin.setMinimumBuyPrice(coin1.get().getMinimumBuyPrice());
                coin.setTransactionFee(coin1.get().getTransactionFee());
                coin.setNew(coin1.get().isNew());
                coin.setTradingSymbol(coin1.get().getTradingSymbol());
                coinRepository.save(coin);
            }
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
        getCoinDetails(coinId);
        return coin.get();
    }

    @Override
    public List<Coin> searchCoin(String keyword) throws Exception {
        List<Coin> bySymbol = coinRepository.findBySymbolContainingIgnoreCase(keyword);
        List<Coin> byName = coinRepository.findByNameContainingIgnoreCase(keyword);

        byName.stream()
                .filter(coin -> !bySymbol.contains(coin))
                .forEach(bySymbol::add);

        return bySymbol;
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
    public List<Coin> getTrendingCoins(int limit) throws Exception {
        List<String> coinList1 = coinRepository.findAllCoinIds();
        String list = coinList1.toString().replaceAll("[\\[\\]]", "").replaceAll("\\s+", "");
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=50&price_change_percentage=1h%2C7d" + "&ids=" + list;
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

            List<Coin> trendingCoins = coinList.stream()
                    .sorted(Comparator.comparingDouble(Coin::getPriceChangePercentage24h).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            return trendingCoins;
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

    @Override
    public String getNews(String categories, Long timestamp) throws Exception {
        String url = "https://min-api.cryptocompare.com/data/v2/news/?lang=EN&lTs=" + timestamp +"&categories=" + categories;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("authorization", "Apikey 38fd1c6f33d6c2a3e3a58c6f3da6295909b420a3e2c3ac23cd5a534309b988a4")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getNewsArticleCategories() throws Exception {
        String url = "https://min-api.cryptocompare.com/data/news/categories";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("authorization", "Apikey 38fd1c6f33d6c2a3e3a58c6f3da6295909b420a3e2c3ac23cd5a534309b988a4")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Coin addCoin(String coinId, double minimumBuyPrice, double transactionFee) throws Exception {
        if(coinRepository.existsById(coinId)) {
            throw new Exception(coinId + " already exists");
        }
        if (minimumBuyPrice <= 0) {
            throw new Exception("Minimum buy price must be greater than 0");
        }

        if (transactionFee < 0) {
            throw new Exception("Transaction fee  must be greater than 0 or = 0");
        }

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

            coin.setMinimumBuyPrice(BigDecimal.valueOf(minimumBuyPrice));
            coin.setTransactionFee(BigDecimal.valueOf(transactionFee));
            coin.setTradingSymbol(coin.getSymbol() + "usdt");

            return coinRepository.save(coin);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Coin updateCoin(String coinId, double minimumBuyPrice, double transactionFee, Long totalSupply) throws Exception {
        if (minimumBuyPrice <= 0) {
            throw new Exception("Minimum buy price must be greater than 0");
        }

        if (transactionFee < 0) {
            throw new Exception("Transaction fee  must be greater than 0 or = 0");
        }
        Coin coin = findById(coinId);
        coin.setMinimumBuyPrice(BigDecimal.valueOf(minimumBuyPrice));
        coin.setTransactionFee(BigDecimal.valueOf(transactionFee));
        coin.setTradingSymbol(coin.getSymbol() + "usdt");

        if(totalSupply != null && totalSupply > 0) {
            UserDTO admin = userService.getUserByEmail(ADMIN_EMAIL);

            AssetDTO oldAsset = assetService.getAssetByUserIdAndCoinIdInternal(internalServiceToken, coin.getId(), admin.getId());
            if (oldAsset == null) {
                CreateAssetRequest assetRequest = new CreateAssetRequest();
                assetRequest.setUserId(admin.getId());
                assetRequest.setCoinId(coinId);
                assetRequest.setQuantity(totalSupply);
                assetService.createAsset(internalServiceToken, assetRequest);
            } else {
                assetService.updateAsset(internalServiceToken, oldAsset.getId(), totalSupply);
            }
        }

        return coinRepository.save(coin);
    }

    @Override
    public void deleteCoin(String coinId) {
        coinRepository.deleteById(coinId);
    }

    @Override
    public Coin updateIsNewStatus(String id, boolean isNew) {
        Optional<Coin> coinOptional = coinRepository.findById(id);
        if (coinOptional.isPresent()) {
            Coin coin = coinOptional.get();
            coin.setNew(isNew);
            return coinRepository.save(coin);
        } else {
            throw new RuntimeException("Coin with ID " + id + " not found");
        }
    }

    @Override
    public Coin updateIsDelistedStatus(String id, boolean isDelisted) {
        Optional<Coin> coinOptional = coinRepository.findById(id);
        if (coinOptional.isPresent()) {
            Coin coin = coinOptional.get();
            coin.setDelisted(isDelisted);
            return coinRepository.save(coin);
        } else {
            throw new RuntimeException("Coin with ID " + id + " not found");
        }
    }

    @Override
    public List<Coin> getDelistedCoins() {
        return coinRepository.findByIsDelistedTrue();
    }

    @Override
    public List<Coin> getNewCoins() {
        return coinRepository.findByIsNewTrue();
    }

    @Override
    public List<String> getTradingSymbols() {
        return coinRepository.findAllTradingSymbols();
    }
}
