package com.duc.user_service.service;

import com.duc.user_service.model.User;
import com.duc.user_service.model.VerificationCode;
import com.duc.user_service.model.VerificationType;

public interface VerificationCodeService {
    VerificationCode sendVerificationCode(User user, VerificationType verificationType);
    VerificationCode getVerificationCodeById(Long id) throws Exception;
    VerificationCode getVerificationCodeByUser(Long userId);
    void deleteVerificationCodeById(VerificationCode verificationCode);
}
