package com.duc.user_service.repository;

import com.duc.user_service.model.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwoFactorOtpRepository extends JpaRepository<TwoFactorOTP, String> {
    TwoFactorOTP findByUserId(Long userId);
}
