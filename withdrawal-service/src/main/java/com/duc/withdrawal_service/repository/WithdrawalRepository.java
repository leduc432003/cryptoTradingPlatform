package com.duc.withdrawal_service.repository;

import com.duc.withdrawal_service.model.Withdrawal;
import com.duc.withdrawal_service.model.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    List<Withdrawal> findByUserId(Long userId);
    List<Withdrawal> findAllByStatus(WithdrawalStatus status);
}
