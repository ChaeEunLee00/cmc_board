package com.cmc.board;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.user.domain.User;
import com.cmc.board.user.repository.UserRepository;
import com.cmc.board.user.dto.UserRequest;
import com.cmc.board.user.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("회원 가입 테스트 (createUser)")
    class CreateUser {

        @Test
        @DisplayName("성공: 중복되지 않은 정보로 가입 요청 시 사용자를 저장한다.")
        void createUser_Success() {
            // given
            UserRequest request = new UserRequest("test@example.com", "password123", "테스터");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

            // when
            userService.createUser(request);

            // then
            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode(anyString());
        }

        @Test
        @DisplayName("실패: 이메일이 중복된 경우 EMAIL_DUPLICATION 예외를 던진다.")
        void createUser_Fail_DuplicateEmail() {
            // given
            UserRequest request = new UserRequest("duplicate@example.com", "password", "nick");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // when & then
            BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                    () -> userService.createUser(request));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.EMAIL_DUPLICATION);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 닉네임이 중복된 경우 NICKNAME_DUPLICATION 예외를 던진다.")
        void createUser_Fail_DuplicateNickname() {
            // given
            UserRequest request = new UserRequest("test@example.com", "password", "duplicateNick");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(true);

            // when & then
            BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                    () -> userService.createUser(request));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.NICKNAME_DUPLICATION);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트 (deleteUser)")
    class DeleteUser {

        @Test
        @DisplayName("성공: 존재하는 이메일인 경우 사용자를 삭제한다.")
        void deleteUser_Success() {
            // given
            String email = "test@example.com";
            User user = User.create(email, "password", "nickname");
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // when
            userService.deleteUser(email);

            // then
            verify(userRepository, times(1)).delete(user);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일인 경우 USER_NOT_FOUND 예외를 던진다.")
        void deleteUser_Fail_NotFound() {
            // given
            String email = "notfound@example.com";
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                    () -> userService.deleteUser(email));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);
            verify(userRepository, never()).delete(any(User.class));
        }
    }
}