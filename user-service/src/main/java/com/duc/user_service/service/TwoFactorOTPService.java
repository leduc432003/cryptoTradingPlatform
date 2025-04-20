package com.duc.user_service.service;

import com.duc.user_service.model.TwoFactorOTP;
import com.duc.user_service.model.User;

import java.time.LocalDateTime;

public interface TwoFactorOTPService {
    TwoFactorOTP createTwoFactorOTP(User user, String otp, String jwt, LocalDateTime expirationTime);
    TwoFactorOTP findByUser(Long userId);
    TwoFactorOTP findById(String id);
    boolean verifyTwoFactorOTP(TwoFactorOTP twoFactorOTP, String otp);
    void deleteTwoFactorOTP(TwoFactorOTP twoFactorOTP);
}
