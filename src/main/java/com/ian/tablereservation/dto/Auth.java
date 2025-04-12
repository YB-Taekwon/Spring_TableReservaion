package com.ian.tablereservation.dto;

import com.ian.tablereservation.entity.UserEntity;
import com.ian.tablereservation.enums.Role;
import lombok.Data;

public class Auth {
    /**
     * 회원 가입 시 클라이언트에서 요청으로 넘어오는 데이터
     * 회원 정보(User)
     */
    @Data
    public static class SignUp {
        private String username;
        private String password;
        private String name;
        private String phone;
        private Role role;


        public UserEntity toEntity() {
            return UserEntity.builder()
                    .username(username)
                    .password(password)
                    .name(name)
                    .phone(phone)
                    .role(role)
                    .build();
        }
    }

    /**
     * 로그인 시 클라이언트에서 요청으로 넘어오는 데이터
     * 아이디, 비밀번호
     */
    @Data
    public static class SignIn {
        private String username;
        private String password;
    }

    @Data
    public static class AuthResponse {
        private User user;
        private Role role;
        private String token;
    }
}
