package org.example.domain.rank.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.member.domain.Member;

@Getter
@AllArgsConstructor
public class RankDataResponse {
    private Member member;
    private Integer totalScore;
}
