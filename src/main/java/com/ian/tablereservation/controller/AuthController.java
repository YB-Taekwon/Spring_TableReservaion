package com.ian.tablereservation.controller;

import com.ian.tablereservation.Service.AuthService;
import com.ian.tablereservation.dto.Auth;
import com.ian.tablereservation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 회원 가입 API
     * 1. 클라이언트에서 사용자 정보 입력 후 회원 가입 시도
     * 2. 서버에서 유효성 검사 및 DB에 데이터 저장
     *
     * @param request: 사용자 정보
     * @return 회원 가입 성공 시 입력한 사용자 정보 그대로 반환
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var user = authService.signup(request);

        log.info("회원 가입 성공 - username: {}", user.getUsername());
        return ResponseEntity.ok(user);
    }


    /**
     * 로그인 API
     * 1. 클라이언트에서 아이디와 비밀번호를 입력하여 로그인 시도
     * 2. 서버에서 유효성 검증 및 사용자 확인
     * 3. 로그인이 성공하면 서버에서 JWT 반환
     *
     * @param request: 아이디, 비밀번호
     * @return 로그인 성공 시 JWT 반환
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var user = authService.signin(request);
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

        Auth.AuthResponse authResponse = new Auth.AuthResponse(user, token);

        log.info("로그인 성공 - username: {}", user.getUsername());
        return ResponseEntity.ok(authResponse);
    }
}
