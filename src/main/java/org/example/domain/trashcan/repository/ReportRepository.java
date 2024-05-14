package org.example.domain.trashcan.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.domain.trashcan.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.domain.member.domain.Member;
import org.example.domain.trashcan.domain.Trashcan;

public interface ReportRepository  extends JpaRepository<Report, Long> {
    boolean existsByMemberAndTrashcan(Member member, Trashcan trashcan);

    long countByMemberAndCreatedAtBetween(Member member, LocalDateTime startOfDay, LocalDateTime endOfDay);

    long countByTrashcan(Trashcan trashcan);

    Integer countByTrashcanId(Long trashcanId);

    Page<Report> findByTrashcanId(Long trashcanId, Pageable pageable);

    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
