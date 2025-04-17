package com.ian.tablereservation.config;

import com.ian.tablereservation.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfiguration: Spring Security 설정 클래스
 * 메서드 단위의 권한 체크 활성화(@PreAuthorize, @PostAuthorize)
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화 (JTW 사용시 필요 X)
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (JTW 사용시 필요 X)
                // 세션을 생성하지 않음 (JWT 사용 시 세션 유지 필요 X, 필수 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 회원가입, 로그인은 모든 권한 허용, 그 외는 인증 필요
                .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/signup", "/auth/signin")
                        .permitAll().anyRequest().authenticated())
                // jwtAuthenticationFilter를 Username/Password 인증 필터 앞에 삽입하여 JWT로 인증 먼저 처리하도록 설정
                // 스프링에서 정의되어 있는 필터 -> 필터의 순서를 정해줌
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager: 로그인 시도 시 인증을 수행해주는 객체
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }
}
