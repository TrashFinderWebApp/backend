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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankService {
    private final ScoreRepository scoreRepository;
    private final MemberRepository memberRepository;
    private List<RankDataResponse> responseDataTop100 = new ArrayList<>();
    private List<RankListResponse> responseList = new ArrayList<>();
    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void updateRankData() {
        responseDataTop100 = scoreRepository.findMemberByScoreDescDTO();
        responseList = convertToRankList(responseDataTop100);
        setEachRank(responseList);
    }

    public List<RankListResponse> getRankList(Integer startIndex, Integer endIndex) {
        if (startIndex == 1 && endIndex >= responseList.size()) {
            return responseList.subList(0, responseList.size());
        }
        if (startIndex == 1) {
            return responseList.subList(0, endIndex);
        }
        if (endIndex >= responseList.size()) {
            return responseList.subList(startIndex - 1, responseList.size());
        }

        return responseList.subList(startIndex - 1, endIndex);
    }

    private void setEachRank(List<RankListResponse> responseSubList) {

        for (int i = 1; i < responseSubList.size(); i++) {
            if (responseSubList.get(i).getTotalScore() == 0) {
                responseSubList.get(i).rankUpdate((long) responseSubList.size());
            }
            Long prevRank = responseSubList.get(i-1).getPersonalRank();
            Long prevPoint = responseSubList.get(i-1).getTotalScore();
            if (responseSubList.get(i).getTotalScore().equals(prevPoint)) {
                responseSubList.get(i).rankUpdate(prevRank);
            }
        }
    }

    private List<RankListResponse> convertToRankList(List<RankDataResponse> responses) {
        List<RankListResponse> listResponses = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            Member member = responses.get(i).getMember();
            Long totalScore = responses.get(i).getTotalScore();
            long personalRank = i + 1;
            listResponses.add(new RankListResponse(
                    member.getId(), member.getName(), totalScore, personalRank));
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
    /* 같은 점수일 때 내림으로 맞춤. 예를 들어 1등이 1명이고 다음 점수가 자신 포함 2명일 시, 3등으로 표기 */
    /* 단, 1등이 여러명이고 자신이 포함된다면 1등으로 표기, 0점은 총 인원 등수(35명이면 35등)으로 표기 */
    private Long convertToPersonalRank(List<RankDataResponse> responseData, Long userPk) {
        long rank = 1;
        long prevTotalScore = Long.MAX_VALUE; // 초기값을 최대값으로 설정
        long sameRankCount = 0;
        long targetScore = -1;

        for (RankDataResponse responseDatum : responseData) {
            if (responseDatum.getMember().getId().equals(userPk)) {
                targetScore = responseDatum.getTotalScore();
            }
            if (responseDatum.getTotalScore() < prevTotalScore) { // 이전 점수보다 작을 때
                if (targetScore >= responseDatum.getTotalScore() && targetScore==responseData.get(0).getTotalScore()) {
                    return rank;
                }
                if (targetScore >= responseDatum.getTotalScore()) {
                    rank += sameRankCount;
                    return rank;
                }
                rank += sameRankCount;
                sameRankCount = 1;
            } else if (responseDatum.getTotalScore() == prevTotalScore) {
                sameRankCount++;
            }
            prevTotalScore = responseDatum.getTotalScore();
        }

        // 마지막 항목 처리
        return rank + sameRankCount - 1;
    }

}
