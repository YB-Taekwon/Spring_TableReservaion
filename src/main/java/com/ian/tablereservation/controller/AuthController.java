package com.ian.tablereservation.controller;

import com.ian.tablereservation.Service.AuthService;
import com.ian.tablereservation.dto.Auth;
import com.ian.tablereservation.dto.User;
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
     * 회원 가입 API
     *
     * @param request
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        User user = authService.register(request);

        return ResponseEntity.ok(user);
    }


    /**
     * 로그인 API
     *
     * @param request
     * @return 로그인 성공 시 JWT 반환
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        String token = authService.authenticate(request);

        return ResponseEntity.ok(token);
    }
}
