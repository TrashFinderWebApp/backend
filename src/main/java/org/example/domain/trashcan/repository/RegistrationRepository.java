package org.example.domain.trashcan.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.domain.member.domain.Member;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    int countByTrashcanId(Long trashcanId);

    Page<Registration> findByMemberId(Long memberId, Pageable pageable);
    List<Registration> findByMemberAndTrashcan(Member member, Trashcan trashcan);
    long countByTrashcan(Trashcan trashcan);

    int countByMemberAndCreatedAtBetween(Member member, LocalDateTime startOfDay, LocalDateTime endOfDay);

}
