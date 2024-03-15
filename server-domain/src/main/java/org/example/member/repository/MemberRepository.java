package org.example.member.repository;

import java.util.Optional;
import org.example.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialId(Long socialId);
}
