package org.example.domain.trashcan.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.example.domain.member.domain.Member;
import org.example.domain.trashcan.domain.Suggestion;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    int countByTrashcanId(Long trashcanId);

    Page<Suggestion> findByMemberId(Long memberId, Pageable pageable);
    List<Suggestion> findByMemberAndTrashcan(Member member, Trashcan trashcan);

    int countByMemberAndCreatedAtBetween(Member member, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT s.trashcan, COUNT(s) as sugCount FROM Suggestion s GROUP BY s.trashcan HAVING COUNT(s) >= :count")
    List<Suggestion> findTrashcansWithSuggestionsCountGreaterThanOrEqual(@Param("count") Integer count);
}
