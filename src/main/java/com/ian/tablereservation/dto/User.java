package com.ian.tablereservation.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String username;
    private String name;
    private String phone;
}
