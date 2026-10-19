package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.MfaRecoveryCode;
import com.example.recomendation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, Long> {
    List<MfaRecoveryCode> findByUserAndUsedFalse(User user);
    void deleteByUser(User user);
}