package com.ian.tablereservation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 1. HTTP 요청 헤더에서 JWT 추출
 * 2. 토큰 유효성 검증
 * 3. 토큰이 유효하면 인증을 SecurityContext에 등록
 * 4. 다음 필터로 요청 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;


    /**
     *
     * @param request
     * @return
     */
    private String resolveToken(HttpServletRequest request) {
        log.info(request.getHeader("Authorization"));

        // 요청 헤더에서 헤더 이름이 Authorization인 값 추출
        String token = request.getHeader(TOKEN_HEADER); // Bearer {token} 형식

        // 조건: Authorization 헤더가 NotBlank이면서 'Bearer '로 시작할 때
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX))
            // 앞에 붙은 'Bearer '를 제외한 나머지 토큰 (순수 JWT 문자열) 추출
            return token.substring(TOKEN_PREFIX.length());

        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 토큰 유효성 검증
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
