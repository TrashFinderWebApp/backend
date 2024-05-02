package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.member.domain.Member;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Suggestion;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    int countByTrashcanId(Long trashcanId);

    List<Suggestion> findByMemberId(Long memberId);
    List<Suggestion> findByMemberAndTrashcan(Member member, Trashcan trashcan);
}
