package org.example.domain.trashcan.repository;

import java.util.Optional;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrashcanRepository extends JpaRepository<Trashcan, Long> {

}
