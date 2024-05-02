package org.example.domain.rank.repository;

import java.util.ArrayList;
import java.util.List;
import org.example.domain.member.domain.Member;
import org.example.domain.rank.controller.dto.RankDataResponse;
import org.example.domain.rank.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query(value = "SELECT m, COALESCE(SUM(s.eachScore), 0) AS totalScore " +
    "FROM Member m LEFT JOIN m.scoreList s "+
    "GROUP BY m.id "+
    "ORDER BY totalScore DESC " +
    "LIMIT 100")
    List<Object[]> findMemberByScoreDesc();

    default List<RankDataResponse> findMemberByScoreDescDTO() {
        List<Object[]> result = findMemberByScoreDesc();
        List<RankDataResponse> responseList = new ArrayList<>();

        for (Object[] row : result) {
            Member member = (Member) row[0];
            Long totalScore = (Long) row[1];
            responseList.add(new RankDataResponse(member, totalScore));
        }

        return responseList;
    }
}
