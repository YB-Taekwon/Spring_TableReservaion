package com.ian.tablereservation.Service;

import com.ian.tablereservation.dto.Auth;
import com.ian.tablereservation.security.JwtTokenProvider;
import com.ian.tablereservation.dto.User;
import com.ian.tablereservation.entity.UserEntity;
import com.ian.tablereservation.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService: 요청으로 인해 직접 호출되는 서비스
 *  예시) 회원 가입, 로그인 -> 로그인 전 인증 요청
 * UserDetailsService: 스프링 내부에서 자동으로 호출되는 서비스
 *  예시) 내 정보 조회 -> 로그인 이후 인증 유지
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원 가입 메서드
     *
     * @param request
     * @return
     */
    public User register(Auth.SignUp request) {
        log.info(request.toString());

        // 아이디 중복 검사
        boolean exists = userRepository.existsByUsername(request.getUsername());
        if (exists)
            throw new RuntimeException("이미 사용 중인 아이디입니다.");

        // 아이디가 중복이 아닌 경우 회원 가입 성공
        request.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        UserEntity user = userRepository.save(request.toEntity());

        log.info(user.toString());

        return user.toDto();
    }


    /**
     * 로그인 시 검증 메서드
     *
     * @param request
     * @return
     */
    public String authenticate(Auth.SignIn request) {
        log.info(request.toString());

        // 아이디 확인 -> 아이디가 존재하지 않으면 예외 발생
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인 -> 비밀번호가 일치하지 않으면 예외 발생
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");

        // 로그인 성공 시 JWT 생성 및 반환
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRole());
    }
}
