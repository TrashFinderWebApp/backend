package org.example.domain.rank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.domain.Member;
import org.example.domain.member.dto.response.ErrorMessage;
import org.example.domain.rank.controller.dto.RankListResponse;
import org.example.domain.rank.service.RankService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "rank", description = "점수별 등수 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController {

    private final RankService rankService;
    @GetMapping("/list")
    @Operation(summary = "랭킹 조회", description = "멤버의 점수 순서대로 반환되는 함수.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "점수 부여 성공"),
            @ApiResponse(responseCode = "404", description = "아무도 점수를 얻지 못함")
    })
    public ResponseEntity<?> getScoreList() {
        try {
            List<RankListResponse> scoreList = rankService.getScoreList();
            return new ResponseEntity<>(scoreList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}
