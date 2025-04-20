package com.duc.service;

import com.duc.dto.CoinDto;
import com.duc.response.ApiResponse;
import com.duc.response.FunctionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
    private final CoinService coinService;
    private static final String GEMINI_API_KEY = "AIzaSyCoyHSK_si1TBs12HEOoqr09xH_0qn-GvY";

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
            Map<String, Object> description = (Map<String, Object>) responseBody.get("description");
            CoinDto coinDto = CoinDto.builder()
                    .id((String) responseBody.get("id"))
                    .name((String) responseBody.get("name"))
                    .symbol((String) responseBody.get("symbol"))
                    .image((String) image.get("large"))
                    .description((String) description.get("en"))
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
        ApiResponse response = new ApiResponse();

        try {
            // Lấy phản hồi chức năng
            FunctionResponse res = getFunctionResponse(prompt);

            // Kiểm tra nếu FunctionResponse là null
            if (res == null) {
                String errorMessage = "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau hoặc liên hệ hỗ trợ qua email: anhducle4433@gmail.com.";
                String friendlyResponse = simpleChat(errorMessage);
                response.setMessage(friendlyResponse);
                return response;
            }

            // Xử lý các tên chức năng khác nhau
            String functionName = res.getFunctionName();
            if ("text_response".equals(functionName)) {
                // Xử lý phản hồi văn bản từ API
                String text = res.getCurrencyName(); // Lấy văn bản từ currencyName
                String friendlyResponse = simpleChat(text); // Tạo phản hồi thân thiện
                response.setMessage(friendlyResponse);
                return response;
            } else if ("getWithdrawalSteps".equals(functionName)) {
                String withdrawalSteps = "Các bước để thực hiện rút tiền:\n" +
                        "1. Đăng nhập vào tài khoản của bạn.\n" +
                        "2. Truy cập vào ví của bạn.\n" +
                        "3. Chọn mục 'Rút tiền' hoặc 'Withdrawal' từ menu.\n" +
                        "4. Nhập số tiền bạn muốn rút và nhập số tài khoản ngân hàng (nếu chưa nhập).\n" +
                        "5. Kiểm tra lại thông tin giao dịch và xác nhận.\n" +
                        "6. Chờ xác nhận từ admin.\n" +
                        "7. Hoàn thành giao dịch và nhận tiền vào tài khoản của bạn.\n" +
                        "Nếu cần hỗ trợ liên hệ email: anhducle4433@gmail.com .\n";
                String friendlyResponse = simpleChat(withdrawalSteps);
                response.setMessage(friendlyResponse);
                return response;
            } else if ("getDepositSteps".equals(functionName)) {
                String depositSteps = "Các bước để thực hiện nạp tiền:\n" +
                        "1. Đăng nhập vào tài khoản của bạn.\n" +
                        "2. Truy cập vào ví của bạn.\n" +
                        "3. Chọn mục 'Nạp tiền' hoặc 'Deposit' từ menu.\n" +
                        "4. Nhập số tiền bạn muốn nạp.\n" +
                        "5. Quét mã QR để chuyển khoản và không được thay đổi thông tin chuyển khoản.\n" +
                        "6. Chờ hệ thống xác nhận và hoàn tất giao dịch.\n" +
                        "7. Tiền sẽ được nạp vào tài khoản của bạn. \n" +
                        "Nếu cần hỗ trợ liên hệ email: anhducle4433@gmail.com .\n";
                String friendlyResponse = simpleChat(depositSteps);
                response.setMessage(friendlyResponse);
                return response;
            } else if ("getTransactionSteps".equals(functionName)) {
                String transactionSteps = "Các bước để thực hiện giao dịch:\n" +
                        "1. Đăng nhập vào tài khoản của bạn.\n" +
                        "2. Truy cập vào coin muốn giao dịch.\n" +
                        "3. Chọn 'Giao dịch' hoặc 'Trade'.\n" +
                        "4. Chọn loại giao dịch bạn muốn thực hiện (Mua hoặc Bán).\n" +
                        "5. Chọn loại lệnh giao dịch phù hợp:\n" +
                        "   - **Lệnh Market**: Mua/Bán ngay với giá thị trường hiện tại.\n" +
                        "   - **Lệnh Limit**: Đặt giá mua/bán mong muốn, lệnh chỉ khớp khi giá thị trường đạt đến mức đó.\n" +
                        "   - **Lệnh Stop-Limit**: Đặt mức giá kích hoạt (Stop Price), khi đạt mức này, một lệnh Limit sẽ được đặt ra.\n" +
                        "6. Nhập số lượng coin bạn muốn giao dịch và xác nhận.\n" +
                        "7. Chờ hệ thống xử lý và hoàn tất giao dịch.\n" +
                        "8. Xem kết quả giao dịch trong tài khoản của bạn.";
                String friendlyResponse = simpleChat(transactionSteps);
                response.setMessage(friendlyResponse);
                return response;
            }

            // Xử lý chức năng getCoinDetails
            String coinId = coinService.getCoinId(res.getCurrencyName().toLowerCase());
            if (coinId == null || coinId.isEmpty()) {
                String errorMessage = "Không tìm thấy thông tin về đồng tiền này. Vui lòng kiểm tra lại tên đồng tiền hoặc thử lại sau.";
                String friendlyResponse = simpleChat(errorMessage);
                response.setMessage(friendlyResponse);
                return response;
            }

            CoinDto apiResponse = makeApiRequest(coinId);

            // Gửi yêu cầu tiếp theo tới Gemini để tạo phản hồi thân thiện
            String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
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
                                                                    .put("content", new ObjectMapper().writeValueAsString(apiResponse))))))))
                    .put("tools", new JSONArray()
                            .put(new JSONObject()
                                    .put("functionDeclarations", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("name", "getCoinDetails")
                                                    .put("description", "Lấy dữ liệu tiền điện tử từ đối tượng tiền tệ được cung cấp.")
                                                    .put("parameters", new JSONObject()
                                                            .put("type", "OBJECT")
                                                            .put("properties", new JSONObject()
                                                                    .put("currencyName", new JSONObject()
                                                                            .put("type", "STRING")
                                                                            .put("description", "Tên tiền tệ, id, ký hiệu."))
                                                                    .put("currencyData", new JSONObject()
                                                                            .put("type", "STRING")
                                                                            .put("description", "Dữ liệu tiền tệ bao gồm id, ký hiệu, giá hiện tại, hình ảnh, mô tả, xếp hạng vốn hóa thị trường, vốn hóa thị trường, khối lượng giao dịch, giá cao 24h, giá thấp 24h, thay đổi giá 24h, phần trăm thay đổi giá 24h, nguồn cung lưu hành, tổng nguồn cung, giá cao nhất mọi thời đại, thêm...")))
                                                            .put("required", new JSONArray()
                                                                    .put("currencyName")
                                                                    .put("currencyData")))))))
                    .toString();

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);

            // Kiểm tra trạng thái HTTP
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                String errorMessage = "Có lỗi khi gọi API. Vui lòng thử lại sau hoặc liên hệ hỗ trợ qua email: anhducle4433@gmail.com.";
                String friendlyResponse = simpleChat(errorMessage);
                response.setMessage(friendlyResponse);
                return response;
            }

            String responseBody = responseEntity.getBody();
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

                    response.setMessage(text);
                    return response;
                }
            }

            // Phản hồi dự phòng nếu không có phản hồi hợp lệ
            String errorMessage = "Không thể lấy thông tin. Vui lòng thử lại hoặc liên hệ hỗ trợ qua email: anhducle4433@gmail.com.";
            String friendlyResponse = simpleChat(errorMessage);
            response.setMessage(friendlyResponse);
            return response;

        } catch (Exception e) {
            // Xử lý mọi ngoại lệ không mong muốn
            System.out.println("Lỗi trong getCoinDetails: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = "Rất tiếc, đã có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại hoặc liên hệ hỗ trợ qua email: anhducle4433@gmail.com.";
            String friendlyResponse = simpleChat(errorMessage);
            response.setMessage(friendlyResponse);
            return response;
        }
    }

    @Override
    public String simpleChat(String prompt) throws JsonProcessingException {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        prompt = "Bạn là một trợ lý cho website giao dịch tiền ảo crypto. Đây là dữ liệu bạn cần trả lời: " + prompt + " .Hãy trả lời khách hàng một cách thật tự nhiên và thân thiện.";
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());

        String text = rootNode.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
        return text;
    }

    public FunctionResponse getFunctionResponse(String prompt) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;
        JSONObject requestBodyJson = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("text", prompt)))))
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
                                                                        .put("description", "The currency name, id, symbol."))
                                                                .put("currencyData", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "Currency Data id, symbol, name, image, description, current price, market cap, market cap rank, fully diluted valuation, total volume, high 24h, price change percentage 1h in currency, price change percentage 7d in currency, low 24h, price change 24h, price change percentage 24h, market cap change 24h, market cap change percentage 24h, circulating supply, total supply, max supply, ath, ath change percentage, ath date, atl, atl change percentage, atl date, last updated.")))
                                                        .put("required", new JSONArray()
                                                                .put("currencyName")
                                                                .put("currencyData"))))
                                        .put(new JSONObject()
                                                .put("name", "getWithdrawalSteps")
                                                .put("description", "Get the steps to perform a withdrawal or way to withdrawal")
                                                .put("parameters", new JSONObject()
                                                        .put("type", "OBJECT")
                                                        .put("properties", new JSONObject()
                                                                .put("prompt", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "The question or prompt for withdrawal steps.")))
                                                        .put("required", new JSONArray().put("prompt"))))
                                        .put(new JSONObject()
                                                .put("name", "getDepositSteps")
                                                .put("description", "Get the steps to perform a deposit or way to deposit")
                                                .put("parameters", new JSONObject()
                                                        .put("type", "OBJECT")
                                                        .put("properties", new JSONObject()
                                                                .put("prompt", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "The prompt for deposit steps.")))
                                                        .put("required", new JSONArray().put("prompt"))))
                                        .put(new JSONObject()
                                                .put("name", "getTransactionSteps")
                                                .put("description", "Get the steps to perform a transaction or way to trading")
                                                .put("parameters", new JSONObject()
                                                        .put("type", "OBJECT")
                                                        .put("properties", new JSONObject()
                                                                .put("prompt", new JSONObject()
                                                                        .put("type", "STRING")
                                                                        .put("description", "The question or prompt for transaction steps.")))
                                                        .put("required", new JSONArray().put("prompt")))))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        String responseBody = response.getBody();

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray candidatesArray = jsonObject.getJSONArray("candidates");
        System.out.println(candidatesArray);

        try {
            if (candidatesArray.length() > 0) {
                JSONObject candidateObject = candidatesArray.getJSONObject(0);
                JSONObject contentObject = candidateObject.getJSONObject("content");
                JSONArray partsArray = contentObject.getJSONArray("parts");

                if (partsArray.length() > 0) {
                    JSONObject partObject = partsArray.getJSONObject(0);

                    // Check if the part contains a functionCall
                    if (partObject.has("functionCall")) {
                        JSONObject functionCallObject = partObject.getJSONObject("functionCall");
                        String functionName = functionCallObject.getString("name");
                        JSONObject argsObject = functionCallObject.getJSONObject("args");

                        if ("getCoinDetails".equals(functionName)) {
                            String currencyData = argsObject.getString("currencyData");
                            String currencyName = argsObject.getString("currencyName");

                            FunctionResponse res = new FunctionResponse();
                            res.setFunctionName(functionName);
                            res.setCurrencyName(currencyName);
                            res.setCurrencyData(currencyData);
                            return res;
                        } else if ("getWithdrawalSteps".equals(functionName)) {
                            String promptValue = argsObject.getString("prompt");

                            System.out.println("Function Name: " + functionName);
                            System.out.println("Prompt: " + promptValue);

                            FunctionResponse res = new FunctionResponse();
                            res.setFunctionName(functionName);
                            res.setCurrencyName(promptValue);
                            return res;
                        } else if ("getDepositSteps".equals(functionName)) {
                            String promptValue = argsObject.getString("prompt");

                            System.out.println("Function Name: " + functionName);
                            System.out.println("Prompt: " + promptValue);

                            FunctionResponse res = new FunctionResponse();
                            res.setFunctionName(functionName);
                            res.setCurrencyName(promptValue);
                            return res;
                        } else if ("getTransactionSteps".equals(functionName)) {
                            String promptValue = argsObject.getString("prompt");

                            FunctionResponse res = new FunctionResponse();
                            res.setFunctionName(functionName);
                            res.setCurrencyName(promptValue);
                            return res;
                        }
                    } else if (partObject.has("text")) {
                        // Handle text response when functionCall is not present
                        String textResponse = partObject.getString("text");

                        FunctionResponse res = new FunctionResponse();
                        res.setFunctionName("text_response"); // Use a generic name or null
                        res.setCurrencyName(textResponse);
                        System.out.println(res);// Store text in currencyName or a new field
                        return res;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing response: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
