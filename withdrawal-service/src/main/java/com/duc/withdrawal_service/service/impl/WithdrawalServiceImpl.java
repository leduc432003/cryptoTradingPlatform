package com.duc.withdrawal_service.service.impl;

import com.duc.withdrawal_service.dto.PaymentDetailsDTO;
import com.duc.withdrawal_service.dto.WalletDTO;
import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;
import com.duc.withdrawal_service.repository.WithdrawalRepository;
import com.duc.withdrawal_service.service.PaymentDetailsService;
import com.duc.withdrawal_service.service.WalletService;
import com.duc.withdrawal_service.service.WithdrawalService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    private final WithdrawalRepository withdrawalRepository;
    private final PaymentDetailsService paymentDetailsService;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @Override
    public Withdrawal requestWithdrawal(String jwt, Long amount, Long userId) throws Exception {
        PaymentDetailsDTO paymentDetails = paymentDetailsService.getUserPaymentDetails(jwt);
        if (paymentDetails == null) {
            throw new Exception("User does not have a linked payment account.");
        }
        WalletDTO walletDTO = walletService.getUserWallet(jwt);
        BigDecimal balance = walletDTO.getBalance();
        BigDecimal withdrawalAmount = BigDecimal.valueOf(amount);
        if(balance.compareTo(withdrawalAmount) < 0) {
            throw new Exception("Insufficient wallet balance for withdrawal.");
        }
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setAmountInVnd(convertUsdToVnd(BigDecimal.valueOf(amount)));
        withdrawal.setUserId(userId);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        return withdrawalRepository.save(withdrawal);
    }

    @Override
    public Withdrawal proceedWithdrawal(Long withdrawalId, boolean accept) throws Exception {
        Optional<Withdrawal> withdrawal = withdrawalRepository.findById(withdrawalId);
        if(withdrawal.isEmpty()) {
            throw new Exception("Withdrawal not found");
        }
        if(withdrawal.get().getStatus() != WithdrawalStatus.PENDING) {
            throw new Exception("Withdrawal is not pending");
        }
        Withdrawal approveWithdrawal = withdrawal.get();
        approveWithdrawal.setDateTime(LocalDateTime.now());
        if(accept) {
            approveWithdrawal.setStatus(WithdrawalStatus.SUCCESS);
        } else {
            approveWithdrawal.setStatus(WithdrawalStatus.DECLINE);
        }
        return withdrawalRepository.save(approveWithdrawal);
    }

    @Override
    public List<Withdrawal> getUsersWithdrawalHistory(Long userId) {
        return withdrawalRepository.findByUserId(userId);
    }

    @Override
    public List<Withdrawal> getAllWithdrawalRequest(WithdrawalStatus withdrawalStatus) {
        if (withdrawalStatus == null) {
            return withdrawalRepository.findAll();
        }
        return withdrawalRepository.findAllByStatus(withdrawalStatus);
    }

    @Override
    public Withdrawal getWithdrawalById(Long withdrawalId) throws Exception {
        Optional<Withdrawal> withdrawal = withdrawalRepository.findById(withdrawalId);
        if(withdrawal.isEmpty()) {
            throw new Exception("Withdraw not found with id + " + withdrawalId);
        }
        return withdrawal.get();
    }

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
}
