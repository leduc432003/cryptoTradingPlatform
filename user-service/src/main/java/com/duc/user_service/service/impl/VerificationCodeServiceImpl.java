package com.duc.user_service.service.impl;

import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationCode;
import com.duc.user_service.model.VerificationType;
import com.duc.user_service.repository.VerificationCodeRepository;
import com.duc.user_service.service.VerificationCodeService;
import com.duc.user_service.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    private final VerificationCodeRepository verificationCodeRepository;

    @Override
    public VerificationCode sendVerificationCode(User user, VerificationType verificationType, LocalDateTime expirationTime) {
        VerificationCode verificationCode1 = VerificationCode.builder()
                .otp(OtpUtils.generateOtp())
                .verificationType(verificationType)
                .user(user)
                .expirationTime(expirationTime)
                .build();

        return verificationCodeRepository.save(verificationCode1);
    }

    @Override
    public VerificationCode getVerificationCodeById(Long id) throws Exception {
        Optional<VerificationCode> verificationCode = verificationCodeRepository.findById(id);
        if(verificationCode.isEmpty()) {
            throw new Exception("don't find verification code with id " + id);
        }
        return verificationCode.get();
    }

    @Override
    public VerificationCode getVerificationCodeByUser(Long userId) {
        return verificationCodeRepository.findByUserId(userId);
    }

    @Override
    public void deleteVerificationCodeById(VerificationCode verificationCode) {
        verificationCodeRepository.delete(verificationCode);
    }
}
