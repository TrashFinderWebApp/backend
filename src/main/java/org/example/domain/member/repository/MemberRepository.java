package org.example.domain.member.repository;

import java.util.Optional;
import org.example.domain.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);

    Page<Member> findByName(String name, Pageable pageable);
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
