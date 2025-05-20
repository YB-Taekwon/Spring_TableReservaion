package com.ian.tablereservation.auth.ui;

import com.ian.tablereservation.auth.application.AuthService;
import com.ian.tablereservation.auth.dto.AuthDto;
import jakarta.validation.Valid;
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


    /**
     * 회원가입 요청을 처리합니다.
     * 클라이언트로부터 전달받은 회원가입 정보를 검증한 후,
     * 회원가입 서비스 로직을 호출하여 회원을 생성하고 응답을 반환합니다.
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 성공 시 사용자 정보 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid AuthDto.SignUpRequest request) {
        log.info("회원가입 요청: 아이디={}", request.getPhone());
        var response = authService.signup(request);

        log.info("회원가입 완료: 아이디={}", response.getPhone());
        return ResponseEntity.ok(response);
    }


    /**
     * 로그인 요청을 처리합니다.
     * 사용자 인증 정보를 검증하고, JWT 토큰을 포함한 로그인 응답을 반환합니다.
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 성공 시 사용자 정보 및 토큰 응답
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody @Valid AuthDto.SignInRequest request) {
        log.info("로그인 요청: 아이디={}", request.getPhone());
        var response = authService.signin(request);

        log.info("로그인 완료: 아이디={}", response.getPhone());
        return ResponseEntity.ok(response);
    }
}
