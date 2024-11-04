package com.duc.wallet_service.service.impl;

import com.duc.wallet_service.model.Wallet;
import com.duc.wallet_service.repository.WalletRepository;
import com.duc.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

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
    public Wallet addBalance(Wallet wallet, Long money) {
        BigDecimal balance = wallet.getBalance();
        BigDecimal newBalance = balance.add(BigDecimal.valueOf(money));
        wallet.setBalance(newBalance);
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
    public Wallet transferToAnotherWallet(Long senderId, Wallet receiverWallet, Long amount) throws Exception {
        Wallet senderWallet = getWalletByUserId(senderId);
        if(senderWallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new Exception("insufficient balance...");
        }
        BigDecimal senderBalance = senderWallet.getBalance().subtract(BigDecimal.valueOf(amount));
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);
        BigDecimal receiverBalance = receiverWallet.getBalance().add(BigDecimal.valueOf(amount));
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(receiverWallet);
        return senderWallet;
    }
}
