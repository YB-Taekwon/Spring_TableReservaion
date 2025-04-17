package com.ian.tablereservation.dto;

import com.ian.tablereservation.entity.UserEntity;
import com.ian.tablereservation.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class Auth {
    /**
     * 회원 가입 시 클라이언트에서 요청으로 넘어오는 데이터
     * 회원 정보(User)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUp {
        @NotBlank
        @Size(min = 11, max = 11, message = "전화번호는 11자리여야 합니다.")
        private String phone;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotNull
        private Role role;


        public UserEntity toEntity() {
            return UserEntity.builder()
                    .phone(phone)
                    .password(password)
                    .name(name)
                    .role(role)
                    .build();
        }
    }

    /**
     * 로그인 시 클라이언트에서 요청으로 넘어오는 데이터
     * 아이디, 비밀번호
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignIn {
        @NotBlank
        private String phone;
        @NotBlank
        private String password;
    }

    /**
     * 로그인 성공 시 반환되는 응답 객체
     * 사용자 정보, 토큰(JWT) 반환
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private User user;
        private String token;
    }


    public static User toDto(UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .phone(userEntity.getPhone())
                .name(userEntity.getName())
                .role(userEntity.getRole())
                .build();
    }
}
