package com.cmc.board;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

/**
 * [Narrative]
 * 관리자나 사용자는 회원 관리 시스템을 통해 계정을 생성하거나 삭제할 수 있어야 한다.
 * 시스템은 중복된 정보를 거부해야 하며, 삭제 시 존재하지 않는 사용자에 대해 예외를 발생시켜야 한다.
 */
@ExtendWith(MockitoExtension.class) // Mockito 기능을 JUnit5에 확장
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService; // Mock들을 주입받은 테스트 대상 객체

    @Nested
    @DisplayName("회원 가입 테스트")
    class CreateUserTest {

        @Test
        @DisplayName("성공: 유효한 정보로 회원가입 시 사용자가 저장되어야 한다.")
        void createUser_Success() {
            // [Given] 새로운 유저 요청 데이터 준비
            UserRequest request = new UserRequest("test@test.com", "password123", "테스터");
            String encodedPassword = "encoded_password";

            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);

            // [When] 회원 가입 실행
            userService.createUser(request);

            // [Then] 검증
            // 1. userRepository.save가 호출되었는지 확인
            // 2. 저장되는 데이터의 비밀번호가 암호화되었는지 확인 (ArgumentCaptor 활용)
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
            assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
            assertThat(savedUser.getUserRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("실패: 이메일 중복 시 EMAIL_DUPLICATION 예외가 발생한다.")
        void createUser_Fail_DuplicateEmail() {
            // [Given] 이미 존재하는 이메일 설정
            UserRequest request = new UserRequest("duplicate@test.com", "password", "nick");
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // [When & Then] 실행 및 예외 검증
            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                userService.createUser(request);
            });

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.EMAIL_DUPLICATION);
            verify(userRepository, never()).save(any(User.class)); // 저장 로직이 호출되면 안 됨
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    class DeleteUserTest {

        @Test
        @DisplayName("성공: 존재하는 이메일로 요청 시 사용자가 삭제되어야 한다.")
        void deleteUser_Success() {
            // [Given] 존재하는 유저 설정
            String email = "delete@test.com";
            User user = new User();
            user.setEmail(email);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            // [When] 회원 탈퇴 실행
            userService.deleteUser(email);

            // [Then] 삭제 메서드 호출 확인
            verify(userRepository, times(1)).delete(user);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 요청 시 USER_NOT_FOUND 예외가 발생한다.")
        void deleteUser_Fail_NotFound() {
            // [Given] 존재하지 않는 이메일
            String email = "none@test.com";
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // [When & Then] 예외 검증
            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                userService.deleteUser(email);
            });

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);
            verify(userRepository, never()).delete(any(User.class));
        }
    }
}