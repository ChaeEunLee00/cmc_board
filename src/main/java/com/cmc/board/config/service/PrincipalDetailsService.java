package com.cmc.board.config.service;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.user.domain.User;
import com.cmc.board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class PrincipalDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws BusinessLogicException {
        // DB에서 이메일로 유저를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        // Security가 이해할 수 있는 UserDetails 객체로 변환해서 반환합니다.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // DB에 저장된 암호화된 비번
                .roles(user.getUserRole().name()) // 유저 권한 (예: USER)
                .build();
    }
}