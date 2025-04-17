package com.ian.tablereservation.repository;

import com.ian.tablereservation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 아이디가 존재하면 UserEntity 반환

    /**
     * 해당 전화번호(ID)를 가진 회원 정보를 조회하는 메서드
     *
     * @param phone: 로그인을 시도하는 ID
     * @return UserEntity: DB에 username이 존재하는 경우
     * Optional.empty(): DB에 username이 존재하지 않는 경우
     */
    Optional<UserEntity> findByPhone(String phone);


    /**
     * 해당 전화번호(ID)가 이미 존재하는지 여부만 확인하는 메서드
     *
     * @param phone : 로그인을 시도하는 ID
     * @return boolean: DB에 username이 존재하는 경우 true, 존재하지 않는 경우 false 반환
     */
    boolean existsByPhone(String phone);
}
