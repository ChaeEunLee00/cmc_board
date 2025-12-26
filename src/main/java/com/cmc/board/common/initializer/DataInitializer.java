package com.cmc.board.common.initializer;

import com.cmc.board.user.domain.User;
import com.cmc.board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // 데이터 저장을 위해 트랜잭션 보장
    public void run(String... args) {
        initAdmin();
    }

    private void initAdmin() {
        String adminEmail = "admin@example.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("=== 관리자 계정이 이미 존재합니다. 생성을 건너뜁니다. ===");
            return;
        }

        // 엔티티 내부의 정적 팩토리 메서드 활용 (객체지향적 생성)
        String encodedPassword = passwordEncoder.encode("admin1234");
        User admin = User.createAdmin(adminEmail, encodedPassword, "관리자");

        userRepository.save(admin);
        log.info("=== 관리자 계정이 생성되었습니다. (ID: {}) ===", adminEmail);
    }
}