package org.example.domain.rank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.domain.rank.controller.dto.PersonalRankResponse;
import org.example.domain.rank.controller.dto.RankListResponse;
import org.example.domain.rank.service.RankService;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "rank", description = "점수별 등수 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController {

    private final RankService rankService;
    private final JwtProvider jwtProvider;

    @GetMapping("/list")
    @Operation(summary = "랭킹 조회", description = "멤버의 점수 순서대로 반환되는 함수.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등수 조회 성공",
                    content = @Content(schema = @Schema(implementation = RankListResponse.class))),
    })
    public ResponseEntity<?> getRankList(
            @RequestParam(name = "startIndex") Integer startIndex,
            @RequestParam(name = "endIndex") Integer endIndex
    ) {
        List<RankListResponse> scoreList = rankService.getRankList(startIndex, endIndex);
        return new ResponseEntity<>(scoreList, HttpStatus.OK);
    }

    @GetMapping("/me")
    @Operation(summary = "본인 랭킹 조회", description = "본인의 랭킹 등수가 반환되는 함수, 회원만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "반환 성공",
                    content = @Content(schema = @Schema(implementation = RankListResponse.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getPersonalRank(HttpServletRequest request) {
        try{
            String token = jwtProvider.resolveAccessToken(request);
            String userPk = jwtProvider.parseClaims(token).getSubject();
            PersonalRankResponse personalRankResponse = rankService.getPersonalRank(userPk);
            return ResponseEntity.ok(personalRankResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorMessage(e.getMessage()));
        }

    }


}
