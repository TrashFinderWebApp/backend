package org.example.domain.rank.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.member.domain.Member;

@AllArgsConstructor
@Getter
public class RankListResponse {
    @Schema(description = "멤버 고유 id", example = "12")
    private Long memberId;
    @Schema(description = "멤버 이름", example = "홍길동")
    private String memberName;
    @Schema(description = "멤버당 총 점수", example = "500")
    private Long totalScore;
}
