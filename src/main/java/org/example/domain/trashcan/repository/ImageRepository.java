package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.trashcan.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByTrashcanId(Long trashcanId);
}
