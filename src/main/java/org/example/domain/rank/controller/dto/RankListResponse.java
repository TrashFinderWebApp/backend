package org.example.domain.rank.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.member.domain.Member;

@AllArgsConstructor
@Getter
public class RankListResponse {
    private Long memberId;
    private String memberName;
    private Integer totalScore;
}
