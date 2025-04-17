package com.ian.tablereservation.service;

import com.ian.tablereservation.dto.Auth;
import com.ian.tablereservation.dto.User;
import com.ian.tablereservation.entity.UserEntity;
import com.ian.tablereservation.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService: 요청으로 인해 직접 호출되는 서비스
 * 예시) 회원 가입, 로그인 -> 로그인 전 인증 요청
 * UserDetailsService: 스프링 내부에서 자동으로 호출되는 서비스
 * 예시) 내 정보 조회 -> 로그인 이후 인증 유지
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입 메서드
    @Transactional
    public User signup(Auth.SignUp request) {
        // 1. 아이디 중복 검사
        if (userRepository.existsByPhone(request.getPhone()))
            throw new RuntimeException("이미 사용 중인 아이디입니다.");

        // 아이디가 중복이 아닌 경우 회원 가입 처리
        request.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        UserEntity user = userRepository.save(request.toEntity());

        return Auth.toDto(user);
    }


    // 로그인 메서드
    public User signin(Auth.SignIn request) {
        // 아이디 확인
        log.info(request.getPhone());
        UserEntity user = findUsername(request);

        // 비밀번호 확인
        validatePassword(request.getPassword(), user.getPassword());

        return Auth.toDto(user);
    }

    // 아이디 확인 -> 아이디가 존재하지 않으면 예외 발생
    private UserEntity findUsername(Auth.SignIn request) {
        return userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 비밀번호 확인 -> 비밀번호가 일치하지 않으면 예외 발생
    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded))
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
    }
}
