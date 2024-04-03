package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;

public interface TrashcanRepository extends JpaRepository<Trashcan, Long> {
    @Query(value = """
                SELECT * FROM (
                    (SELECT *
                    FROM trashcan
                    WHERE ST_Distance_Sphere(location, :location) < :radius
                    ORDER BY ST_Distance_Sphere(location, :location) ASC
                    LIMIT 15)
                    UNION
                    (SELECT *
                    FROM trashcan
                    WHERE ST_Distance_Sphere(location, :location) < :radius
                    ORDER BY views DESC
                    LIMIT 15)
                ) AS combined_results
                """, nativeQuery = true)
    List<Trashcan> findWithinDistance(@Param("location") Point location, @Param("radius") double radius);
}
