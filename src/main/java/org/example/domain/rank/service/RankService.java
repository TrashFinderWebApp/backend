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

    public List<RankListResponse> getRankList(Integer startIndex, Integer endIndex, Long lastRank, Long lastScore) {
        List<RankDataResponse> responseData = scoreRepository.findMemberByScoreDescDTO();
        log.info(String.valueOf(responseData.size()));
        List<RankListResponse> responseList = convertToRankList(responseData);

        if (startIndex == 1) {
            return setEachRank(responseList.subList(0, endIndex), lastRank, lastScore);
        }
        if (endIndex >= responseList.size()) {
            return setEachRank(responseList.subList(startIndex - 1, responseList.size()), lastRank, lastScore);
        }

        return setEachRank(responseList.subList(startIndex - 1, endIndex), lastRank, lastScore);

    }

    private List<RankListResponse> setEachRank(
            List<RankListResponse> responseSubList, Long lastRank, Long lastScore) {

        for (int i = 0; i < responseSubList.size(); i++) {
            if (responseSubList.get(i).getTotalScore() == 0) {
                responseSubList.get(i).rankUpdate(0L);
            }
            if (i == 0 && responseSubList.get(i).getTotalScore().equals(lastScore)) {
                responseSubList.get(i).rankUpdate(lastRank);
                continue;
            }
            if (i == 0) {
                continue;
            }
            Long prevRank = responseSubList.get(i-1).getPersonalRank();
            Long prevPoint = responseSubList.get(i-1).getTotalScore();
            if (responseSubList.get(i).getTotalScore().equals(prevPoint)) {
                responseSubList.get(i).rankUpdate(prevRank);
            }
        }
        return responseSubList;
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


    /* 같은 점수일 때 같은 등수 */
//    private Long convertToPersonalRank(List<RankDataResponse> responseData, Long userPk) {
//        long rank = 1;
//        long prevTotalScore = 0;
//        long sameRankCount = 0;
//        long targetScore = -1;
//
//        for (int i = 0; i < responseData.size(); i++) {
//            if (responseData.get(i).getMember().getId().equals(userPk)) {
//                targetScore = responseData.get(i).getTotalScore();
//            }
//            if (responseData.get(i).getTotalScore() != prevTotalScore) {
//                if (targetScore >= prevTotalScore) {
//                    return rank;
//                }
//                rank += sameRankCount;
//                sameRankCount = 1;
//            }
//            else {
//                sameRankCount++;
//            }
//
//            prevTotalScore = responseData.get(i).getTotalScore();
//        }
//        return 0L;
//    }
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
