package org.example.domain.trashcan.repository;

import java.util.List;
import org.example.domain.trashcan.domain.Trashcan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                    AND status = :status
                    ORDER BY ST_Distance_Sphere(location, :location) ASC
                    LIMIT 15)
                    UNION
                    (SELECT *
                    FROM trashcan
                    WHERE ST_Distance_Sphere(location, :location) < :radius
                    AND status = :status
                    ORDER BY views DESC
                    LIMIT 15)
                ) AS combined_results
                """, nativeQuery = true)
    List<Trashcan> findWithinDistance(@Param("location") Point location, @Param("radius") double radius, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM trashcan WHERE ST_Equals(location, :location) = true", nativeQuery = true)
    Long existsByLocation(@Param("location") Point location);

    List<Trashcan> findByStatus(String status);

    @Query("SELECT t FROM Trashcan t LEFT JOIN Registration r ON t.id = r.trashcan.id " +
            "WHERE t.status = :status " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(r) DESC")
    Page<Trashcan> findByStatusSortedByRegistrationCount(@Param("status") String status, Pageable pageable);
    @Query("SELECT t FROM Trashcan t LEFT JOIN Suggestion s ON t.id = s.trashcan.id " +
            "WHERE t.status = :status " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(s) DESC")
    Page<Trashcan> findByStatusSortedBySuggestionCount(@Param("status") String status, Pageable pageable);
    @Query("SELECT t FROM Trashcan t LEFT JOIN Report r ON t.id = r.trashcan.id " +
            "WHERE t.status = :status " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(r) DESC")
    Page<Trashcan> findByStatusSortedByReportCount(@Param("status") String status, Pageable pageable);


}
