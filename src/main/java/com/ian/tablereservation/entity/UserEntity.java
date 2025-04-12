package com.ian.tablereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ian.tablereservation.dto.User;
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
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "user")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 테이블 아이디

    @JsonIgnore
    private String password; // 비밀번호

    private String username; // 로그인 아이디
    private String name; // 이름
    private String phone; // 전화번호

    @Enumerated(EnumType.STRING)
    private Role role;

    public User toDto() {
        return User.builder()
                .username(username)
                .name(name)
                .phone(phone)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
