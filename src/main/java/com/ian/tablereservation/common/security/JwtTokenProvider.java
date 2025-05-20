package com.ian.tablereservation.common.security;

import com.ian.tablereservation.common.enums.Role;
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
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;

    private final CustomUserDetailsService customUserDetailsService;


    @Value("${spring.jwt.secret-key}")
    private String secretKeyString;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));
    }

    public String generateToken(String phone, Role role) {
        log.info("토큰 생성 처리");
        var now = new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        log.info("토큰 생성 처리 성공");
        return Jwts.builder()
                .subject(phone)
                .claim(KEY_ROLE, role)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getClaims(String token) {
        log.info("토큰 파싱 처리");
        try {
            log.info("토큰 파싱 처리 성공");
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("토큰 파싱 처리 중 오류 발생: {}", e.getMessage(), e);
            return e.getClaims();
        }
    }

    public String getUsername(String token) {
        return this.getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        log.info("토큰 유효성 검증 처리");
        if (!StringUtils.hasText(token)) return false;

        var claims = getClaims(token);

        log.info("토큰 유효성 검증 완료");
        return !claims.getExpiration().before(new Date());
    }


    public Authentication getAuthentication(String jwt) {
        String phone = getUsername(jwt);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(phone);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
