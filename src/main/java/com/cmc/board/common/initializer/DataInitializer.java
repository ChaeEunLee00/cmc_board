package com.cmc.board.common.initializer;

import com.cmc.board.user.User;
import com.cmc.board.user.UserRepository;
import com.cmc.board.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 이미 admin 계정이 있는지 확인
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin1234")); // 비밀번호 암호화
            admin.setNickname("관리자");
            admin.setUserRole(UserRole.ADMIN); // 앞서 설정한 Enum 사용
            admin.setCreatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("=== 관리자 계정이 생성되었습니다. (admin@example.com) ===");
        }
    }
}