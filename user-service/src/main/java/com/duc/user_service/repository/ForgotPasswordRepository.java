package com.duc.user_service.repository;

import com.duc.user_service.model.ForgotPasswordOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPasswordOTP, String> {
    ForgotPasswordOTP findByUserId(Long userId);
}
