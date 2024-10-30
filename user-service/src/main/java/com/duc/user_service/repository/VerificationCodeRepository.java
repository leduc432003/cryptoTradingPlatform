package com.duc.user_service.repository;

import com.duc.user_service.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByUserId(Long userId);
}
