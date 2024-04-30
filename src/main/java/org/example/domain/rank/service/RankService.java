package org.example.domain.rank.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.Member;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.rank.controller.dto.PersonalRankResponse;
import org.example.domain.rank.controller.dto.RankDataResponse;
import org.example.domain.rank.controller.dto.RankListResponse;
import org.example.domain.rank.repository.ScoreRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankService {
    private final ScoreRepository scoreRepository;
    private final MemberRepository memberRepository;

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

    public PersonalRankResponse getPersonalRank(String userPk) {
        List<RankDataResponse> responseData = scoreRepository.findMemberByScoreDescDTO();

        long totalCount = memberRepository.count();
        long personalRank = convertToPersonalRank(responseData, Long.parseLong(userPk));
        long personalPoint = responseData.stream()
                .filter(data -> data.getMember().getId().equals(Long.parseLong(userPk)))
                .findFirst()
                .map(RankDataResponse::getTotalScore)
                .orElse(0L);

        if (personalRank == 0L) {
            personalRank = totalCount;
        }

        return new PersonalRankResponse(personalRank, totalCount, personalPoint);
    }

    private Long convertToPersonalRank(List<RankDataResponse> responseData, Long userPk) {
        long rank = 1;
        long prevTotalScore = 0;
        long sameRankCount = 0;
        long targetScore = -1;

        for (int i = 0; i < responseData.size(); i++) {
            if (responseData.get(i).getMember().getId().equals(userPk)) {
                targetScore = responseData.get(i).getTotalScore();
            }
            if (responseData.get(i).getTotalScore() != prevTotalScore) {
                if (targetScore >= prevTotalScore) {
                    return rank;
                }
                rank += sameRankCount;
                sameRankCount = 1;
            }
            else {
                sameRankCount++;
            }

            prevTotalScore = responseData.get(i).getTotalScore();
        }
        return 0L;
    }


}
