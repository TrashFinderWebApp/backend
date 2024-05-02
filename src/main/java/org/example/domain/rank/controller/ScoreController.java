package org.example.domain.rank.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.domain.rank.service.ScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "score", description = "점수관련 획득 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/suggestion/add")
    @Operation(summary = "제안 점수 추가", description = "제안한 위치가 등록되면 점수가 추가된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "점수 부여 성공"),
    })
    public ResponseEntity<?> addRegistrationScore(Long trashcanId) {
        //trashcan Id에 묶여있는 member id 리스트 조회
        //멤버당 점수 추가
        //모든 멤버 성공 시 OK
        //한명이라도 실패시 롤백 후 실패 반환

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
