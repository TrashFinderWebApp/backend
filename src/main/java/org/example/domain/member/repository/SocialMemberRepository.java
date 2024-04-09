package org.example.domain.member.repository;

import java.util.Optional;
import org.example.domain.member.domain.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialMemberRepository extends JpaRepository<SocialMember, Long> {
    boolean existsBySocialId(String socialId);
    Optional<SocialMember> findBySocialId(String socialId);
}
