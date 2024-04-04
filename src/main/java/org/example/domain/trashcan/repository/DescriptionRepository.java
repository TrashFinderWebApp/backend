package org.example.domain.trashcan.repository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByTrashcanId(Long trashcanId);
}
