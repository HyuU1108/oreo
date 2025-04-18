package com.oreo.repository;

import com.oreo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email); // 존재 여부 확인 메소드

    boolean existsByNickname(String nickname); // 존재 여부 확인 메소드
}