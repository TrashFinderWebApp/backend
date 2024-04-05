package org.example.domain.rank.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.Member;
import org.example.domain.rank.controller.dto.RankDataResponse;
import org.example.domain.rank.controller.dto.RankListResponse;
import org.example.domain.rank.repository.ScoreRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankService {
    private final ScoreRepository scoreRepository;

    public List<RankListResponse> getScoreList() {
        List<RankDataResponse> responseData = scoreRepository.findMemberByScoreDescDTO();
        if (responseData == null) {
            throw new IllegalArgumentException("resources not found");
        }
        return convertToRankList(responseData);
    }

    private List<RankListResponse> convertToRankList(List<RankDataResponse> responses) {
        List<RankListResponse> listResponses = new ArrayList<>();

        for (RankDataResponse row : responses) {
            Member member = row.getMember();
            Long totalScore = row.getTotalScore();
            listResponses.add(new RankListResponse(member.getId(), member.getName(), totalScore));
        }

        return listResponses;
    }


}
