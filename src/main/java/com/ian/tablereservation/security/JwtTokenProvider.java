package com.ian.tablereservation.security;

import com.ian.tablereservation.enums.Role;
import com.ian.tablereservation.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final String KEY_ROLE = "role";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1시간

    private final UserService userService;


    @Value("${spring.jwt.secret}")
    private String secretKeyString;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));
    }

    /**
     * 주어진 사용자 명과 권한을 기반으로 JWT 토큰을 생성하는 메서드
     *
     * @param phone: 사용자 아이디 (전화번호)
     * @param role:  권한
     * @return JWT 토큰을 문자열 형식으로 반환
     */
    public String generateToken(String phone, Role role) {
        var now = new Date(); // 현재 시간
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME); // 토큰 만료 간

        return Jwts.builder()
                .subject(phone) // 토큰의 주제(subject) 설정 -> 보통 사용자의 ID
                .claim(KEY_ROLE, role) // 커스텀 클레임 추가 -> 사용자 권한 삽입
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256) // 토큰 서명 설정 -> 키와 알고리즘으로 암호화
                .compact(); // 위의 모든 설정을 바탕으로 최종 토큰 문자열 생성
    }


    /**
     * 토큰을 분석해서 Claims를 반환하는 메서드
     * JWT 토큰에서 Claims(페이로드 정보)를 안전하게 추출한다.
     *
     * @param token: 사용자가 보낸 JWT 문자열 (보통 Authorization 헤더에 담긴 문자열)
     * @return Claims: JWT 내부에 담긴 정보를 꺼내는 객체 (예: 유저 아이디, 권한, 발급일 등)
     */
    private Claims getClaims(String token) {
        log.info("Token to be parsed={}", token);
        // 토큰 분석 중 생길 수 있는 오류(토큰 만료 등)를 잡기 위해 예외 처리 사용
        try {
            return Jwts.parser()
                    // JWT의 서명 검증을 위한 키 설정
                    .verifyWith(secretKey)
                    // 파서 인스턴스 생성
                    .build()
                    // 토큰을 파싱하고 서명까지 확인
                    .parseSignedClaims(token)
                    // 파싱 결과에서 페이로드(Claims) 부분만 꺼냄
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰은 만료됐지만 Claims는 꺼낼 수 있음
            return e.getClaims();
        }
    }


    public String getUsername(String token) {
        return this.getClaims(token).getSubject();
    }


    /**
     * JWT 토큰이 유효한지 검사하는 메서드
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        // 토큰이 비어있거나 null이면 유효하지 않음 -> false 반환
        if (!StringUtils.hasText(token)) return false;

        // 토큰에서 Claims(내용)를 추출 (만료된 토큰이라도 Claims는 가져올 수 있음)
        var claims = getClaims(token);

        // 토큰의 만료시간(expiration)이 현재 시간보다 이전이면 토큰 만료 -> false 반환
        // 그렇지 않으면 유효한 토큰 -> true 반환
        return !claims.getExpiration().before(new Date());
    }


    public Authentication getAuthentication(String jwt) {
        String phone = getUsername(jwt);
        UserDetails userDetails = userService.loadUserByUsername(phone);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
