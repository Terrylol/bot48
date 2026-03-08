package com.cyber48.backend.repo;

import com.cyber48.backend.entity.UserEntity;
import com.cyber48.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findByRole(UserRole role);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
