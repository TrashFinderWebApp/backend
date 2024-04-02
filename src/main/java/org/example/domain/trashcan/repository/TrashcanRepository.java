package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;

public interface TrashcanRepository extends JpaRepository<Trashcan, Long> {
    @Query("from Trashcan t where distance(t.location, :location) < :radius")
    List<Trashcan> findWithinDistance(@Param("location") Point location, @Param("radius") double radius);
}
