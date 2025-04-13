package com.duc.payment_service.service.impl;

import com.duc.payment_service.dto.WalletTransactionType;
import com.duc.payment_service.dto.request.AddBalanceRequest;
import com.duc.payment_service.model.Payment;
import com.duc.payment_service.model.PaymentStatus;
import com.duc.payment_service.repository.PaymentRepository;
import com.duc.payment_service.service.PaymentService;
import com.duc.payment_service.service.WalletService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
    @Value("${internal.service.token}")
    private String internalServiceToken;

    private BigDecimal convertUsdToVnd(BigDecimal amountInUsd) throws Exception {
        String apiUrl = "https://api.coingecko.com/api/v3/coins/tether";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .header("x-cg-demo-api-key", "CG-HfJNVa7kfaaTEWbWDmjDQnWM")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());

            BigDecimal vndRate = new BigDecimal(jsonNode.path("market_data").path("current_price").path("vnd").asText());

            if (vndRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid VND rate received from API");
            }

            BigDecimal amountInVnd = amountInUsd.multiply(vndRate);
            return amountInVnd.setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new Exception("Error occurred while calling CoinGecko API: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Payment createPaymentRequest(Long userId, BigDecimal amount) throws Exception {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAccountNumber("0972024254");
        payment.setBank("MBBank");
        String content = "THANHTOAN " + userId + " " + UUID.randomUUID();
        payment.setContent(content);
        payment.setAmount(amount);
        payment.setAmountInVnd(convertUsdToVnd(amount));
        payment.setStatus(PaymentStatus.PENDING);
        String qrLink = generateQrLink(userId, convertUsdToVnd(amount), content);
        payment.setQrLink(qrLink);
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public boolean checkPaymentStatus(Long paymentId, Long userId) throws Exception {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (!paymentOptional.isPresent()) {
            throw new IllegalArgumentException("Payment not found");
        }

        Payment payment = paymentOptional.get();
        if (!payment.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this payment");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment.getStatus() == PaymentStatus.SUCCESS;
        }

        BigDecimal amountInVnd = payment.getAmountInVnd();
        BigDecimal amount = payment.getAmount();
        String qrContent = payment.getContent().toUpperCase().replace("-", "");

        String apiUrl = "https://my.sepay.vn/userapi/transactions/list?account_number=0972024254&limit=20";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer ZQILW4AGL9SKIGQ6QXQJPLJSWCMEJOOLYOYZRAC0AUSND3CFTTUV1EREJYBVHADK")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());
            JsonNode transactionsNode = jsonNode.path("transactions");
            for (JsonNode transactionNode : transactionsNode) {
                String transactionContent = transactionNode.path("transaction_content").asText().toUpperCase();
                BigDecimal amountIn = new BigDecimal(transactionNode.path("amount_in").asText());

                if (transactionContent.contains(qrContent) && amountIn.compareTo(amountInVnd) == 0) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    AddBalanceRequest addBalanceRequest = new AddBalanceRequest();
                    addBalanceRequest.setUserId(payment.getUserId());
                    addBalanceRequest.setMoney(amount.doubleValue());
                    addBalanceRequest.setTransactionType(WalletTransactionType.ADD_MONEY);
                    walletService.addBalance(internalServiceToken, addBalanceRequest);
                    paymentRepository.save(payment);
                    return true;
                }
            }
            if (payment.getCreatedAt().plusMinutes(30).isBefore(LocalDateTime.now())) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<Payment> getPayments(Long userId) throws Exception {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Payment getPaymentById(Long userId, Long paymentId) throws Exception {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isEmpty()) {
            throw new Exception("Payment not found");
        }

        Payment payment = paymentOptional.get();
        if (!payment.getUserId().equals(userId)) {
            throw new Exception("You are not authorized to view this payment");
        }

        return payment;
    }

    private String generateQrLink(Long userId, BigDecimal amount, String content) {
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s",
                "0972024254", "MBBank", amount, content);
    }
}
