package org.example.domain.trashcan.repository;

import org.example.domain.trashcan.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    int countByTrashcanId(Long trashcanId);
}
