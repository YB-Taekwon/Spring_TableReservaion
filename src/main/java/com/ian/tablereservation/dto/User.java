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
    private String phone;
    private String name;
    private Role role;
}
