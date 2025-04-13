package com.ian.tablereservation.dto;

import com.ian.tablereservation.enums.Role;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Role role;
}
