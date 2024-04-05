package org.example.domain.member.repository;

import org.example.domain.member.domain.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialMemberRepository extends JpaRepository<SocialMember, Long> {
    boolean existsBySocialId(String socialId);
}
