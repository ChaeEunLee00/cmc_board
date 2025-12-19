package com.cmc.board.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.AntPathMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable) // 테스트 편의를 위해 해제
                .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // For H2 DB
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/users/signup", "/login").permitAll() // 가입, 로그인은 허용
                        .anyRequest().authenticated() // 나머지는 로그인 필수
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // POST /login 요청 시 Security가 로그인 진행
                        .usernameParameter("email")   // 아이디 대신 이메일 사용
                        .passwordParameter("password")
                        // ⭐️ 로그인 성공 시 302 대신 200 응답 반환
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/text;charset=UTF-8");
                            response.getWriter().write("로그인에 성공하였습니다.");
                        })
                        // ⭐️ 로그인 실패 시 302 대신 401(또는 200) 응답 반환
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 또는 SC_OK
                            response.setContentType("application/text;charset=UTF-8");
                            response.getWriter().write("로그인에 실패하였습니다: " + exception.getMessage());
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")         // POST /logout 요청 시 세션 무효화
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/text;charset=UTF-8");
                            response.getWriter().write("로그아웃에 성공하였습니다.");
                        })
                );

        return http.build();
    }

    // passwordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
