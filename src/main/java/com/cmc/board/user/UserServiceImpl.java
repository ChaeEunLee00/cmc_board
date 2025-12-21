package com.cmc.board.user;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UserRequest request) {
        validateDuplication(request.getEmail(), request.getNickname());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(request.getEmail(), encodedPassword, request.getNickname());

        userRepository.save(user);
    }

    @Override
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    private void validateDuplication(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessLogicException(ExceptionCode.EMAIL_DUPLICATION);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_DUPLICATION);
        }
    }
}

