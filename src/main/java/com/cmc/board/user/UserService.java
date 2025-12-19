package com.cmc.board.user;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRequest request){
        // 이메일이 중복되는지 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessLogicException(ExceptionCode.EMAIL_DUPLICATION);
        }

        // 닉네임이 중복되는지 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_DUPLICATION);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 암호화하여 저장
        user.setNickname(request.getNickname());
        user.setUserRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public void deleteUser(String email){
        // 존재하는지 확인
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
