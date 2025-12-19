package com.cmc.board.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController { // 로그인, 인가, 로그아웃은 spring security의 필터체인에서 처리

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity signup(@Valid @RequestBody UserRequest request) {
        userService.createUser(request);
        return new ResponseEntity<>("회원가입이 완료되었습니다.", HttpStatus.OK);
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity deleteUser(Principal principal) {
        userService.deleteUser(principal.getName());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
