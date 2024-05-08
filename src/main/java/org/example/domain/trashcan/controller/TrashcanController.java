package org.example.domain.trashcan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.request.TrashcanLocationRequest;
import org.example.domain.trashcan.dto.request.TrashcanStatusRequest;
import org.example.domain.trashcan.dto.response.PersonalTrashcansResponse;
import org.example.domain.trashcan.dto.response.ReportResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponseWithReportCount;
import org.example.domain.trashcan.dto.response.TrashcanLocationResponse;
import org.example.domain.trashcan.dto.response.TrashcanMessageResponse;
import org.example.domain.trashcan.exception.InvalidStatusException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.example.domain.trashcan.service.TrashcanService;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "trashcan", description = "쓰레기통 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trashcan")
public class TrashcanController {

    private final TrashcanService trashcanService;
    private final JwtProvider jwtProvider;


    @GetMapping("/locations")
    @Operation(summary = "쓰레기통 찾기", description = "반경 내 쓰레기통 찾기",
            parameters = {
                    @Parameter(name = "radius", description = "반경(m)", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = {
                            @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TrashcanLocationResponse.class))
                            )
                    }
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 status 값이거나, 해당 조건에 맞는 쓰레기통이 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "해당 조건에 맞는 쓰레기통이 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getTrashcanLocations(
            @ModelAttribute TrashcanLocationRequest requestDto) {
        List<String> validStatuses = Arrays.asList("added", "registered", "suggested", "removed");
        if (!validStatuses.contains(requestDto.getStatus())) {
            throw new InvalidStatusException("유효하지 않은 status 값입니다.");
        }

        List<TrashcanLocationResponse> responseList = trashcanService.findTrashcanLocations(
                requestDto);

        return ResponseEntity.ok().body(responseList);
    }

    @GetMapping("/locations/details/{id}")
    @Operation(summary = "쓰레기통 정보", description = "쓰레기통 상세정보")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 상세정보 가져오기 성공",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanDetailsResponse.class))}),
            @ApiResponse(responseCode = "404", description = "쓰레기통 정보를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<TrashcanDetailsResponse> getTrashcanDetails(@PathVariable("id") Long id) {
        TrashcanDetailsResponse response = trashcanService.getTrashcanDetailsResponse(id);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/registrations")
    @Operation(summary = "쓰레기통 등록", description = "새로운 쓰레기통 위치를 등록합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 위치 등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "쓰레기통 좌표 중복 or 이미지 저장 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
    public ResponseEntity<?> registerTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "addressDetail", required = false) String addressDetail,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageObject", required = false) List<MultipartFile> imageObjects) {

        String token = jwtProvider.resolveAccessToken(request);
        TrashcanMessageResponse response = trashcanService.registerTrashcan(latitude, longitude,
                addressDetail, address, description, imageObjects, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/registrations/{id}")
    @Operation(summary = "등록 쓰레기통 정보 추가", description = "기존 쓰레기통의 정보를 추가합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 정보 추가 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 or 이미지 저장 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
    public ResponseEntity<?> registerTrashcanId(
            @PathVariable("id") Long trashcanId,
            HttpServletRequest request,
            @RequestParam(value = "imageObject", required = false) List<MultipartFile> imageObjects,
            @RequestParam(value = "description", required = false) String description) {
        String token = jwtProvider.resolveAccessToken(request);
        trashcanService.registerTrashcanId(trashcanId, imageObjects, description, token);
        return ResponseEntity.ok().body(new TrashcanMessageResponse("정보 추가 성공"));
    }

    @PostMapping("/suggestions")
    @Operation(summary = "쓰레기통 위치 제안", description = "쓰레기통이 설치되길 원하는 위치를 제안합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 위치 제안 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "쓰레기통 좌표 중복 or 이미지 저장 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
    public ResponseEntity<?> suggestTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "addressDetail", required = false) String addressDetail,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageObject", required = false) List<MultipartFile> imageObjects) {
        String token = jwtProvider.resolveAccessToken(request);
        TrashcanMessageResponse response = trashcanService.suggestTrashcan(latitude, longitude,
                addressDetail, address, description, imageObjects, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/suggestions/{id}")
    @Operation(summary = "제안 쓰레기통 정보 추가", description = "기존 쓰레기통의 정보를 추가합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 정보 추가 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 or 이미지 저장 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
    public ResponseEntity<?> suggestTrashcanId(
            @PathVariable("id") Long trashcanId,
            HttpServletRequest request,
            @RequestParam(value = "imageObject", required = false) List<MultipartFile> imageObjects,
            @RequestParam(value = "description", required = false) String description) {
        String token = jwtProvider.resolveAccessToken(request);
        trashcanService.suggestTrashcanId(trashcanId, imageObjects, description, token);
        return ResponseEntity.ok().body(new TrashcanMessageResponse("정보 추가 성공"));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "본인이 등록, 위치 제안한 쓰레기통 정보 가져오기",
            description = "본인이 등록, 위치 제안한 쓰레기통 정보 가져오기. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 가져오기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonalTrashcansResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터(회원 id)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
    public ResponseEntity<List<PersonalTrashcansResponse>> getTrashcansDetailsByMemberId(
            @PathVariable Long memberId) {
        List<PersonalTrashcansResponse> trashcanDetails = trashcanService.getTrashcanDetailsByMemberId(
                memberId);
        if (trashcanDetails.isEmpty()) {
            throw new TrashcanNotFoundException("등록하거나 위치 제안한 쓰레기통이 없습니다.");
        }
        return ResponseEntity.ok().body(trashcanDetails);
    }

    @PatchMapping("/status/{id}")
    @Operation(summary = "쓰레기통 상태 변경",
            description = "쓰레기통 상태 변경. 이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 상태 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TrashcanMessageResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> updateTrashcanStatus(@PathVariable Long id,
            @RequestBody TrashcanStatusRequest request) {
        List<String> validStatuses = Arrays.asList("added", "registered", "suggested", "removed");
        if (!validStatuses.contains(request.getStatus())) {
            throw new InvalidStatusException("유효하지 않은 status 값입니다.");
        }
        trashcanService.updateTrashcanStatus(id, request.getStatus());
        return ResponseEntity.ok().body(new TrashcanMessageResponse("성공적으로 쓰레기통의 상태를 변경했습니다."));
    }

    @GetMapping("/list")
    @Operation(summary = "쓰레기통 리스트 조회 with 요청횟수",
            description = "상태와 요청횟수를 받아서 요청횟수 이상인 쓰레기통 리스트 조회. 이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TrashcanDetailsResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> getTrashcansByStatusAndCount(
            @RequestParam String status,
            @RequestParam Integer count) {
        List<String> validStatuses = Arrays.asList("added", "registered", "suggested", "removed");
        if (!validStatuses.contains(status)) {
            throw new InvalidStatusException("유효하지 않은 status 값입니다.");
        }
        if (count <= 0) {
            throw new InvalidStatusException("잘못된 count 값입니다.");
        }
        List<TrashcanDetailsResponseWithReportCount> trashcansResponseList = trashcanService.getTrashcanDetailsByStatusAndCount(
                status, count);
        if (trashcansResponseList.isEmpty()) {
            throw new TrashcanNotFoundException("조건을 만족하는 쓰레기통이 없습니다.");
        }
        return ResponseEntity.ok().body(trashcansResponseList);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "쓰레기통 삭제",
            description = "쓰레기통 삭제. 이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> deleteTrashcan(@PathVariable Long id) {
        trashcanService.deleteTrashcanById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reports/{id}")
    @Operation(summary = "쓰레기통 신고 추가", description = "쓰레기통에 대한 신고를 추가합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 신고 추가 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> reportTrashcan(
            @PathVariable("id") Long trashcanId,
            HttpServletRequest request,
            @RequestParam(value = "description", required = true) String description) {
        String token = jwtProvider.resolveAccessToken(request);
        trashcanService.reportTrashcan(trashcanId, description, token);
        return ResponseEntity.ok().body(new TrashcanMessageResponse("신고가 성공적으로 등록되었습니다."));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "쓰레기통 신고 내용 조회", description = "쓰레기통에 대한 신고 요청 내용을 확인합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 신고 요청 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonalTrashcansResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<List<ReportResponse>> getReportsByTrashcanId(@PathVariable Long id) {
        List<ReportResponse> responses = trashcanService.getReportsByTrashcanId(id);
        return ResponseEntity.ok(responses);
    }
}
