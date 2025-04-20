package com.duc.user_service.service;

import com.duc.user_service.model.ForgotPasswordOTP;
import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationType;

import java.time.LocalDateTime;

public interface ForgotPasswordService {
    ForgotPasswordOTP createOTP(User user, String id, String otp, VerificationType verificationType, String sendTo, LocalDateTime expirationTime);
    ForgotPasswordOTP findById(String id);
    ForgotPasswordOTP findByUser(Long userId);
    void deleteOTP(ForgotPasswordOTP otp);
}
