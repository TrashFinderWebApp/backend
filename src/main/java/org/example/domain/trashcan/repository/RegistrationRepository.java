package org.example.domain.trashcan.repository;

import java.util.List;
import java.util.Optional;
import org.example.domain.member.domain.Member;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    int countByTrashcanId(Long trashcanId);

    List<Registration> findByMemberId(Long memberId);

    List<Registration> findByMemberAndTrashcan(Member member, Trashcan trashcan);
}
