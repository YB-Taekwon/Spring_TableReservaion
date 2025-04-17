package com.ian.tablereservation.service;

import com.ian.tablereservation.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService: 스프링 시큐리티 내부에서 자동으로 호출되며,
 * 매 요청마다 JWT에 담긴 사용자 정보를 바탕으로 인증 객체를 구성하는 데 사용
 *  예시) 내 정보 조회 -> 로그인 이후 인증을 유지
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return this.userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
