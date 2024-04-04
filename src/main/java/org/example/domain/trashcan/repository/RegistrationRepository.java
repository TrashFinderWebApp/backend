package org.example.domain.trashcan.repository;

import org.example.domain.trashcan.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    int countByTrashcanId(Long trashcanId);
}
