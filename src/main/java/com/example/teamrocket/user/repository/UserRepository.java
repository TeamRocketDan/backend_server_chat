package com.example.teamrocket.user.repository;

import com.example.teamrocket.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUuid(String uuid);
}
