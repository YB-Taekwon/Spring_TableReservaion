package com.ian.tablereservation.user.domain;

import com.ian.tablereservation.common.enums.Role;
import com.ian.tablereservation.common.base.BaseEntity;
import com.ian.tablereservation.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    List<Store> stores = new ArrayList<>();

    public void encodePassword(String password) {
        this.password = password;
    }
}
