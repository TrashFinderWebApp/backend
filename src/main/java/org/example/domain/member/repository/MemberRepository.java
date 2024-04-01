package org.example.domain.member.repository;

import java.util.Optional;
import org.example.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialId(String socialId);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
