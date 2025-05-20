package com.ian.tablereservation.auth.application;

import com.ian.tablereservation.auth.dto.AuthDto;
import com.ian.tablereservation.common.security.JwtTokenProvider;
import com.ian.tablereservation.user.domain.User;
import com.ian.tablereservation.user.domain.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 회원 가입 요청을 처리합니다.
     * 요청한 전화번호가 이미 존재하는 경우 예외를 발생시키며,
     * 그렇지 않은 경우 비밀번호를 인코딩한 뒤 사용자 정보를 저장합니다.
     *
     * @param request 회원 가입 요청 정보
     * @return 회원 가입 응답 DTO
     * @throws RuntimeException 이미 등록된 전화번호인 경우 발생
     */
    @Transactional
    public AuthDto.SignUpResponse signup(AuthDto.SignUpRequest request) {
        log.info("회원가입 요청 처리 시작: 아이디={}", request.getPhone());

        if (userRepository.existsByPhone(request.getPhone())) {
            log.error("회원가입 실패 - 중복된 아이디: {}", request.getPhone());
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        User user = AuthDto.SignUpRequest.from(request);
        user.encodePassword(passwordEncoder.encode(user.getPassword()));
        log.debug("비밀번호 암호화 완료: 아이디={}", user.getPhone());

        User result = userRepository.save(user);

        log.info("회원가입 성공: 사용자 ID={}, 아이디={}", result.getId(), result.getPhone());
        return AuthDto.SignUpResponse.from(result);
    }


    /**
     * 사용자 로그인 요청을 처리합니다.
     * 아이디 또는 비밀번호가 일치하지 않는 경우 예외를 발생시키며,
     * 그렇지 않은 경우 JWT 토큰을 발급합니다.
     *
     * @param request 로그인 요청 정보
     * @return 로그인 응답 DTO (JWT 토큰 포함)
     * @throws UsernameNotFoundException 사용자가 존재하지 않는 경우
     * @throws BadCredentialsException 비밀번호가 일치하지 않는 경우
     */
    public AuthDto.SignInResponse signin(AuthDto.SignInRequest request) {
        log.info("로그인 요청 수신: 아이디={}", request.getPhone());

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> {
                    log.error("로그인 실패 - 존재하지 않는 사용자: 아이디={}", request.getPhone());
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("로그인 실패 - 비밀번호 불일치: 아이디={}", request.getPhone());
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getPhone(), user.getRole());

        log.info("로그인 성공: 사용자 ID={}, 아이디={}", user.getId(), user.getPhone());
        return AuthDto.SignInResponse.from(user, token);
    }
}
