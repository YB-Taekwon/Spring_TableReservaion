package com.ian.tablereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ian.tablereservation.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "user")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 테이블 아이디

    @JsonIgnore
    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(unique = true, nullable = false)
    private String phone; // 로그인 아이디 -> 전화번호

    @Column(nullable = false)
    private String name; // 이름

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // 권한


    // 사용자가 가진 권한 목록을 반환 -> 스프링 시큐리티가 해당 권한을 바탕으로 인가 처리
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // 사용자의 비밀번호를 반환 -> 로그인 시 암호화 된 비밀번호와 비교
    @Override
    public String getPassword() {
        return password;
    }

    // 로그인 시 입력하는 username(ID)을 반환 -> 전화번호를 아이디로 사용
    @Override
    public String getUsername() {
        return phone;
    }

    // 계정의 만료 여부 -> 계정이 오래되어 만료되었을 경우 false로 설정
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정의 잠금 여부 -> 로그인 시도 실패가 여러 번일 경우 false로 설정하여 계정 잠금
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 -> 주기적인 비밀번호 변경 정책에 사용
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정의 활성화 여부 -> 휴면 계정의 경우 false로 설정
    @Override
    public boolean isEnabled() {
        return true;
    }
}
