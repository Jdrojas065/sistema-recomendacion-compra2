package com.example.recomendation.security.mfa;

import com.example.recomendation.model.entity.MfaRecoveryCode;
import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.repository.MfaRecoveryCodeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoveryCodeService {

    private static final int CODE_COUNT = 8;
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    private final MfaRecoveryCodeRepository recoveryCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public List<String> regenerate(User user) {
        recoveryCodeRepository.deleteByUser(user);

        List<String> plainCodes = new ArrayList<>();
        for (int i = 0; i < CODE_COUNT; i++) {
            String raw = generateRawCode();
            String display = raw.substring(0, 5) + "-" + raw.substring(5);
            plainCodes.add(display);

            recoveryCodeRepository.save(MfaRecoveryCode.builder()
                    .user(user)
                    .codeHash(passwordEncoder.encode(raw))
                    .used(false)
                    .build());
        }
        return plainCodes;
    }

    @Transactional
    public boolean verifyAndConsume(User user, String inputCode) {
        if (inputCode == null || inputCode.isBlank()) return false;
        String normalized = inputCode.trim().toUpperCase().replace("-", "");

        for (MfaRecoveryCode candidate : recoveryCodeRepository.findByUserAndUsedFalse(user)) {
            if (passwordEncoder.matches(normalized, candidate.getCodeHash())) {
                candidate.setUsed(true);
                candidate.setUsedAt(LocalDateTime.now());
                recoveryCodeRepository.save(candidate);
                return true;
            }
        }
        return false;
    }

    public long countRemaining(User user) {
        return recoveryCodeRepository.findByUserAndUsedFalse(user).size();
    }

    private String generateRawCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}