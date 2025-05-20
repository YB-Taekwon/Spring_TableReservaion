package com.ian.tablereservation.auth.dto;

import com.ian.tablereservation.user.domain.User;
import com.ian.tablereservation.common.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpRequest {

        @NotBlank
        @Size(min = 11, max = 11, message = "전화번호는 11자리여야 합니다.")
        private String phone;

        @NotBlank
        private String password;

        @NotBlank
        private String name;

        @NotNull
        private Role role;

        public static User from(SignUpRequest request) {
            return User.builder()
                    .phone(request.getPhone())
                    .password(request.getPassword())
                    .name(request.getName())
                    .role(request.getRole())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignInRequest {

        @NotBlank
        private String phone;

        @NotBlank
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpResponse {
        private String phone;
        private String name;
        private Role role;

        public static SignUpResponse from(User user) {
            return SignUpResponse.builder()
                    .phone(user.getPhone())
                    .name(user.getName())
                    .role(user.getRole())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignInResponse {
        private String phone;
        private String name;
        private Role role;
        private String token;

        public static SignInResponse from(User user, String token) {
            return SignInResponse.builder()
                    .phone(user.getPhone())
                    .name(user.getName())
                    .role(user.getRole())
                    .token(token)
                    .build();
        }
    }
}
