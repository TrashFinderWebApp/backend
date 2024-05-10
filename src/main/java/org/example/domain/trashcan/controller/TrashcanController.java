package org.example.domain.trashcan.controller;

import io.jsonwebtoken.Claims;
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
import org.example.domain.trashcan.dto.response.PersolnalTrashcansPageResponse;
import org.example.domain.trashcan.dto.response.PersonalTrashcansResponse;
import org.example.domain.trashcan.dto.response.ReportListResponse;
import org.example.domain.trashcan.dto.response.ReportResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsPageResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponseWithReportCount;
import org.example.domain.trashcan.dto.response.TrashcanLocationResponse;
import org.example.domain.trashcan.dto.response.TrashcanMessageResponse;
import org.example.domain.trashcan.exception.InvalidStatusException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.example.domain.trashcan.service.TrashcanService;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/trashcans")
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
        List<String> validStatuses = Arrays.asList("added", "REGISTERED", "SUGGESTED", "REMOVED");
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

    @GetMapping("/members/{id}")
    @Operation(summary = "특정 멤버가 등록, 위치 제안한 쓰레기통 정보 가져오기",
            description = "특정 멤버가 등록, 위치 제안한 쓰레기통 정보 가져오기, 타입은 (REGISTRATION, SUGGESTION). 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 가져오기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersolnalTrashcansPageResponse.class))
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
    public ResponseEntity<?> getTrashcansDetailsByMemberId(@PathVariable Long id, @RequestParam String type,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PersolnalTrashcansPageResponse trashcanDetails = trashcanService.getTrashcanDetailsByMemberId(id, type, pageable);
        if (trashcanDetails.getPersonalTrashcansResponses().isEmpty()) {
            throw new TrashcanNotFoundException("등록하거나 위치 제안한 쓰레기통이 없습니다.");
        }
        return ResponseEntity.ok().body(trashcanDetails);
    }
    @GetMapping("/members/me")
    @Operation(summary = "본인이 등록, 위치 제안한 쓰레기통 정보 가져오기",
            description = "본인이 등록, 위치 제안한 쓰레기통 정보 가져오기. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 가져오기 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersolnalTrashcansPageResponse.class))
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
    public ResponseEntity<?> getTrashcansDetailsByMe(HttpServletRequest request, @RequestParam String type,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size){
        String token = jwtProvider.resolveAccessToken(request);
        Claims claims = jwtProvider.parseClaims(token);
        Long memberId = Long.parseLong(claims.getSubject());

        Pageable pageable = PageRequest.of(page, size);
        PersolnalTrashcansPageResponse trashcanDetails = trashcanService.getTrashcanDetailsByMemberId(memberId, type, pageable);
        if (trashcanDetails.getPersonalTrashcansResponses().isEmpty()) {
            throw new TrashcanNotFoundException("등록하거나 위치 제안한 쓰레기통이 없습니다.");
        }
        return ResponseEntity.ok().body(trashcanDetails);

    }

    @PostMapping("/reports/{id}")
    @Operation(summary = "쓰레기통 신고 추가",
            description = "쓰레기통에 대한 신고를 추가합니다. 하루에 3번 신고 가능, 중복 신고 불가능, 신고 5회 이상 자동 REMOVED. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
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

}
