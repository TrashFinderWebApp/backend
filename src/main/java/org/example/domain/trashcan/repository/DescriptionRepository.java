package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.trashcan.domain.Description;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescriptionRepository extends JpaRepository<Description, Long> {
    List<Description> findByTrashcanId(Long trashcanId);
}
