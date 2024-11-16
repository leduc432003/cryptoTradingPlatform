package com.duc.wallet_service.service.impl;

import com.duc.wallet_service.dto.OrderDTO;
import com.duc.wallet_service.dto.OrderType;
import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.model.WalletTransactionType;
import com.duc.wallet_service.repository.WalletRepository;
import com.duc.wallet_service.service.OrderService;
import com.duc.wallet_service.service.WalletService;
import com.duc.wallet_service.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final OrderService orderService;
    private final WalletTransactionService walletTransactionService;

    @Override
    public Wallet getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if(wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.valueOf(100));
            walletRepository.save(wallet);
        }
        return wallet;
    }

    @Override
    public Wallet addBalance(Wallet wallet, double money, WalletTransactionType transactionType) {
        BigDecimal balance = wallet.getBalance();
        BigDecimal newBalance = balance.add(BigDecimal.valueOf(money));
        wallet.setBalance(newBalance);
        walletTransactionService.createWalletTransaction(wallet, transactionType, null, transactionType.toString(), BigDecimal.valueOf(money));
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findWalletById(Long walletId) throws Exception {
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        if(wallet.isPresent()) {
            return wallet.get();
        }
        throw new Exception("wallet not found.");
    }

    @Override
    public Wallet transferToAnotherWallet(Long senderId, Wallet receiverWallet, BigDecimal amount) throws Exception {
        Wallet senderWallet = getWalletByUserId(senderId);
        if(senderWallet.getBalance().compareTo(amount) < 0) {
            throw new Exception("insufficient balance...");
        }
        BigDecimal senderBalance = senderWallet.getBalance().subtract(amount);
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);
        BigDecimal receiverBalance = receiverWallet.getBalance().add(amount);
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(receiverWallet);
        return senderWallet;
    }

    @Override
    public Wallet payOrderPayment(Long orderId, Long userId, String jwt) throws Exception {
        Wallet wallet = getWalletByUserId(userId);
        OrderDTO order = orderService.getOrderById(jwt, orderId);
        BigDecimal newBalance;
        if(order.getOrderType().equals(OrderType.BUY)) {
            newBalance = wallet.getBalance().subtract(order.getPrice());
            if(newBalance.compareTo(order.getPrice()) < 0) {
                throw new Exception("Not enough money for this transaction");
            }
            walletTransactionService.createWalletTransaction(wallet, WalletTransactionType.BUY_ASSET, null, "BUY ASSET", order.getPrice());
        } else {
            newBalance = wallet.getBalance().add(order.getPrice());
            walletTransactionService.createWalletTransaction(wallet, WalletTransactionType.SELL_ASSET, null, "SELL ASSET", order.getPrice());
        }
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }
}
