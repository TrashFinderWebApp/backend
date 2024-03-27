package org.example.domain.user.repository;

import java.util.Optional;
import org.example.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId);
    boolean existsByEmail(String email);
}
