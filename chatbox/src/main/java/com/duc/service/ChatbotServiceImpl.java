package com.duc.service;

import com.duc.dto.CoinDto;
import com.duc.response.ApiResponse;
import com.duc.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    String GEMINI_API_KEY = "AIzaSyBr-JZjwWieU_ms_1WSBYixTbuulnPxhtw";

    private double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
    }

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

    public CoinDto makeApiRequest(String currencyName) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/" + currencyName;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = responseEntity.getBody();
        if(responseBody != null) {
            Map<String, Object> image = (Map<String, Object>) responseBody.get("image");
            Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");
            CoinDto coinDto = CoinDto.builder()
                    .id((String) responseBody.get("id"))
                    .name((String) responseBody.get("name"))
                    .symbol((String) responseBody.get("symbol"))
                    .image((String) image.get("large"))
                    //market data
                    .currentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")))
                    .marketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")))
                    .marketCapRank(convertToDouble(convertToDouble((int) marketData.get("market_cap_rank"))))
                    .totalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")))
                    .high24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")))
                    .low24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")))

                    .priceChange24h(convertToDouble((marketData.get("price_change_24h"))))
                    .priceChangePercentage24h(convertToDouble((marketData.get("price_change_percentage_24h"))))
                    .priceChangePercentage1hInCurrency(convertToDouble(((Map<String, Object>) marketData.get("price_change_percentage_1h_in_currency")).get("usd")))
                    .priceChangePercentage7dInCurrency(convertToDouble(((Map<String, Object>) marketData.get("price_change_percentage_7d_in_currency")).get("usd")))
                    .marketCapChange24h(convertToDouble((marketData.get("market_cap_change_24h"))))
                    .marketCapChangePercentage24(convertToDouble((marketData.get("market_cap_change_percentage_24h"))))
                    .circulatingSupply(convertToDouble((marketData.get("circulating_supply"))))
                    .totalSupply(convertToDouble((marketData.get("total_supply"))))

                    .ath(convertToDouble(((Map<String, Object>) marketData.get("ath")).get("usd")))
                    .athChangePercentage(convertToDouble(((Map<String, Object>) marketData.get("ath_change_percentage")).get("usd")))
                    .atl(convertToDouble(((Map<String, Object>) marketData.get("atl")).get("usd")))
                    .atlChangePercentage(convertToDouble(((Map<String, Object>) marketData.get("atl_change_percentage")).get("usd")))
                    .build();

            Map<String, String> athDateData = (Map<String, String>) marketData.get("ath_date");
            if (athDateData != null) {
                String athDateUsd = athDateData.get("usd");
                if (athDateUsd != null) {
                    Date athDate = parseIsoDate(athDateUsd);
                    System.out.println("ATH Date: " + athDate);
                    coinDto.setAthDate(athDate);
                } else {
                    System.err.println("ath_date for USD is missing");
                    coinDto.setAthDate(null);
                }
            } else {
                System.err.println("ath_date data is missing");
                coinDto.setAthDate(null);
            }

            Map<String, String> atlDateData = (Map<String, String>) marketData.get("atl_date");
            if (atlDateData != null) {
                String atlDateUsd = atlDateData.get("usd");
                if (atlDateUsd != null) {
                    Date atlDate = parseIsoDate(atlDateUsd);
                    System.out.println("ATL Date: " + atlDate);
                    coinDto.setAtlDate(atlDate);
                } else {
                    System.err.println("atl_date for USD is missing");
                    coinDto.setAtlDate(null);
                }
            } else {
                System.err.println("atl_date data is missing");
                coinDto.setAtlDate(null);
            }

            String lastUpdated = (String) responseBody.get("last_updated");
            if (lastUpdated != null) {
                Date lastUpdatedDate = parseIsoDate(lastUpdated);
                System.out.println("Last Updated: " + lastUpdatedDate);
                coinDto.setLastUpdated(lastUpdatedDate);
            } else {
                System.err.println("Last updated date is missing");
                coinDto.setLastUpdated(null);
            }

            return coinDto;
        }
        throw new Exception("coin not found");
    }

    @Override
    public ApiResponse getCoinDetails(String prompt) throws Exception {
        FunctionResponse res = getFunctionResponse(prompt);
        CoinDto apiResponse = makeApiRequest(res.getCurrencyName().toLowerCase());
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + GEMINI_API_KEY;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("text", prompt))))
                        .put(new JSONObject()
                                .put("role", "model")
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("functionCall", new JSONObject()
                                                        .put("name", "getCoinDetails")
                                                        .put("args", new JSONObject()
                                                                .put("currencyName", res.getCurrencyName())
                                                                .put("currencyData", res.getCurrencyData()))))))
                        .put(new JSONObject()
                                .put("role", "function")
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("functionResponse", new JSONObject()
                                                        .put("name", "getCoinDetails")
                                                        .put("response", new JSONObject()
                                                                .put("name", "getCoinDetails")
                                                                .put("content", apiResponse))))))
                )
                .put("tools", new JSONArray()
                        .put(new JSONObject()
                                .put("functionDeclarations", new JSONArray()
                                .put(new JSONObject()
                                        .put("name", "getCoinDetails")
                                        .put("description", "Get crypto currency data from given currency object.")
                                        .put("parameters", new JSONObject()
                                                .put("type", "OBJECT")
                                                .put("properties", new JSONObject()
                                                        .put("currencyName", new JSONObject()
                                                                .put("type", "STRING")
                                                                .put("description", "The currency Name, " +
                                                                        "id, symbol."))
                                                        .put("currencyData", new JSONObject()
                                                                .put("type", "STRING")
                                                                .put("description",
                                                                        "The currency data id, " +
                                                                                "symbol, current price, " +
                                                                                "image, " +
                                                                                "market cap rank, " +
                                                                                "market cap extra, " +
                                                                                "total volume, " +
                                                                                "high 24h, " +
                                                                                "low 24h, " +
                                                                                "price change 24h, " +
                                                                                "price change percentage 24h, " +
                                                                                "circulating supply, " +
                                                                                "total supply, " +
                                                                                "ath, " +
                                                                                "extra..."
                                                                )))
                                                .put("required", new JSONArray()
                                                        .put("currencyName")
                                                        .put("currencyData")))))))
                .toString();
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);
        String responseBody = response.getBody();
        System.out.println("-------" + responseBody);
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray candidatesArray = jsonObject.getJSONArray("candidates");

        if (candidatesArray.length() > 0) {
            JSONObject candidateObject = candidatesArray.getJSONObject(0);
            JSONObject contentObject = candidateObject.getJSONObject("content");
            JSONArray partsArray = contentObject.getJSONArray("parts");

            if (partsArray.length() > 0) {
                JSONObject partObject = partsArray.getJSONObject(0);
                String text = partObject.getString("text");

                ApiResponse ans = new ApiResponse();
                ans.setMessage(text);
                return ans;
            }
        }
        return null;
    }

    @Override
    public String simpleChat(String prompt) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + GEMINI_API_KEY;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("text", prompt)))))
                .toString();
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        return response.getBody();
    }

    public FunctionResponse getFunctionResponse(String prompt) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + GEMINI_API_KEY;
        JSONObject requestBodyJson = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("text", prompt)
                                        ))))
                .put("tools", new JSONArray()
                        .put(new JSONObject()
                                .put("functionDeclarations", new JSONArray()
                                        .put(new JSONObject()
                                                .put("name", "getCoinDetails")
                                                .put("description", "Get the coin details from given currency object")
                                                .put("parameters", new JSONObject()
                                                        .put("type", "OBJECT")
                                                        .put("properties", new JSONObject()
                                                                .put("currencyName", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "The currency name, " +
                                                                                "id, symbol."))
                                                                .put("currencyData", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "Currency Data id, " +
                                                                                "symbol, " +
                                                                                "name, " +
                                                                                "image, " +
                                                                                "current_price," +
                                                                                "market_cap, " +
                                                                                "market_cap_rank, " +
                                                                                "fully_diluted_valuation, " +
                                                                                "total_volume, high_24h, " +
                                                                                "price_change_percentage_1h_in_currency, " +
                                                                                "price_change_percentage_7d_in_currency, " +
                                                                                "low_24h, price_change_24h, " +
                                                                                "price_change_percentage_24h, " +
                                                                                "market_cap_change_24h, " +
                                                                                "market_cap_change_percentage_24h, " +
                                                                                "circulating_supply, " +
                                                                                "total_supply, " +
                                                                                "max_supply, " +
                                                                                "ath, " +
                                                                                "ath_change_percentage, " +
                                                                                "ath_date, " +
                                                                                "atl, " +
                                                                                "atl_change_percentage, " +
                                                                                "atl_date, last_updated."
                                                                                )))
                                                        .put("required", new JSONArray()
                                                                .put("currencyName")
                                                                .put("currencyData")))))))
                ;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        String responseBody = response.getBody();

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray candidatesArray = jsonObject.getJSONArray("candidates");

        if (candidatesArray.length() > 0) {
            JSONObject candidateObject = candidatesArray.getJSONObject(0);
            JSONObject contentObject = candidateObject.getJSONObject("content");
            JSONArray partsArray = contentObject.getJSONArray("parts");

            if (partsArray.length() > 0) {
                JSONObject partObject = partsArray.getJSONObject(0);
                JSONObject functionCallObject = partObject.getJSONObject("functionCall");

                String functionName = functionCallObject.getString("name");
                JSONObject argsObject = functionCallObject.getJSONObject("args");

                String currencyData = argsObject.getString("currencyData");
                String currencyName = argsObject.getString("currencyName");

                // Output the values
                System.out.println("Function Name: " + functionName);
                System.out.println("Currency Data: " + currencyData);
                System.out.println("Currency Name: " + currencyName);
                FunctionResponse res = new FunctionResponse();
                res.setFunctionName(functionName);
                res.setCurrencyName(currencyName);
                res.setCurrencyData(currencyData);
                return res;
            }
        }
        return null;
    }
}
