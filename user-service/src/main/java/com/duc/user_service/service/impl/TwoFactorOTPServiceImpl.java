package com.duc.user_service.service.impl;

import com.duc.user_service.model.TwoFactorOTP;
import com.duc.user_service.model.User;
import com.duc.user_service.repository.TwoFactorOtpRepository;
import com.duc.user_service.service.TwoFactorOTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorOTPServiceImpl implements TwoFactorOTPService {
    private final TwoFactorOtpRepository twoFactorOtpRepository;

    @Override
    public TwoFactorOTP createTwoFactorOTP(User user, String otp, String jwt) {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        TwoFactorOTP twoFactorOTP = TwoFactorOTP.builder()
                .otp(otp)
                .jwt(jwt)
                .id(id)
                .user(user)
                .build();
        return twoFactorOtpRepository.save(twoFactorOTP);
    }

    @Override
    public TwoFactorOTP findByUser(Long userId) {
        return twoFactorOtpRepository.findByUserId(userId);
    }

    @Override
    public TwoFactorOTP findById(String id) {
        Optional<TwoFactorOTP> otp = twoFactorOtpRepository.findById(id);
        return otp.orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOTP(TwoFactorOTP twoFactorOTP, String otp) {
        return twoFactorOTP.getOtp().equals(otp);
    }

    @Override
    public void deleteTwoFactorOTP(TwoFactorOTP twoFactorOTP) {
        twoFactorOtpRepository.delete(twoFactorOTP);
    }
}
