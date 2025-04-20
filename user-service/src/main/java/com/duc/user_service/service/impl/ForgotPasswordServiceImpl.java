package com.duc.user_service.service.impl;

import com.duc.user_service.model.ForgotPasswordOTP;
import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationType;
import com.duc.user_service.repository.ForgotPasswordRepository;
import com.duc.user_service.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;

    @Override
    public ForgotPasswordOTP createOTP(User user, String id, String otp, VerificationType verificationType, String sendTo, LocalDateTime expirationTime) {
        ForgotPasswordOTP forgotPasswordOTP = ForgotPasswordOTP.builder()
                .id(id)
                .user(user)
                .sendTo(sendTo)
                .otp(otp)
                .verificationType(verificationType)
                .expirationTime(expirationTime)
                .build();
        return forgotPasswordRepository.save(forgotPasswordOTP);
    }

    @Override
    public ForgotPasswordOTP findById(String id) {
        Optional<ForgotPasswordOTP> forgotPasswordOTP = forgotPasswordRepository.findById(id);
        return forgotPasswordOTP.orElse(null);
    }

    @Override
    public ForgotPasswordOTP findByUser(Long userId) {
        return forgotPasswordRepository.findByUserId(userId);
    }

    @Override
    public void deleteOTP(ForgotPasswordOTP otp) {
        forgotPasswordRepository.delete(otp);
    }
}
