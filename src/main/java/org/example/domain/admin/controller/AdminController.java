package org.example.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.domain.admin.controller.dto.response.MemberListResponse;
import org.example.domain.admin.service.AdminService;
import org.example.domain.notification.controller.dto.request.CreateNotificationRequest;
import org.example.domain.notification.controller.dto.response.NotificationListResponseAll;
import org.example.domain.trashcan.dto.request.TrashcanStatusRequest;
import org.example.domain.trashcan.dto.response.PersolnalTrashcansPageResponse;
import org.example.domain.trashcan.dto.response.ReportListResponse;
import org.example.domain.trashcan.dto.response.ReportResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsPageResponse;
import org.example.domain.trashcan.dto.response.TrashcanMessageResponse;
import org.example.domain.trashcan.exception.InvalidStatusException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.example.domain.trashcan.service.TrashcanService;
import org.example.global.advice.ErrorMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "admin", description = "관리자 API")
public class AdminController {
    private final AdminService adminService;
    private final TrashcanService trashcanService;
    @GetMapping("/members")
    @Operation(summary = "멤버 리스트 조회", description = "멤버 리스트 API, 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token")
    public ResponseEntity<?> getMembersList(@RequestParam Integer page, @RequestParam(required = false) String memberName) {
        try {
            MemberListResponse memberListResponse;

            if (memberName == null) {
                memberListResponse = adminService.getMembersList(page);
            }
            else {
                memberListResponse = adminService.getMembersListFindByName(page, memberName);
            }

            return new ResponseEntity<>(memberListResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/members/{id}")
    @Operation(summary = "멤버 역할 변경", description = "멤버 역할 변경 API, 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token")
    public ResponseEntity<?> updateMemberRole(@PathVariable("id") Long memberId, @RequestParam String updatedRole) {
        try {
            adminService.updateMemberRole(memberId, updatedRole);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "특정 쓰레기통 신고 내용 조회", description = "쓰레기통에 대한 신고 요청 내용을 확인합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 신고 요청 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReportResponse.class))
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

    @GetMapping("/reports")
    @Operation(summary = "쓰레기통 신고 리스트 조회",
            description = "쓰레기통 신고 리스트 최신순으로 조회, 페이지 사이즈 20. 이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 신고 요청 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReportListResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<ReportListResponse> getReports(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        ReportListResponse reportListResponse = trashcanService.getReports(pageable);
        return ResponseEntity.ok(reportListResponse);
    }

    @DeleteMapping("/reports/{id}")
    @Operation(summary = "쓰레기통 신고 요청 삭제",
            description = "쓰레기통 신고 요청 삭제. 이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 신고 요청 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {

        trashcanService.deleteReportById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/trashcans/{id}")
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

    @GetMapping("/trashcans")
    @Operation(summary = "쓰레기통 리스트 정렬해서 조회",
            description = "상태와 정렬 조건을 받아서 쓰레기통 리스트 조회, 상태는 (ADDED, REGISTERED, SUGGESTED, REMOVED)"
                    + "sort는 (ASC, DESC)으로 REMOVED를 제외하고 조회수를 기준으로 함, REMOVED는 신고 수가 기준, 페이지 기본 사이즈 20.  이 작업은 관리자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 리스트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TrashcanDetailsPageResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "404", description = "데이터가 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))),
    })
    public ResponseEntity<?> getTrashcansByStatus(@RequestParam String status,
            @RequestParam String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        List<String> validStatuses = Arrays.asList("added", "REGISTERED", "SUGGESTED", "REMOVED");
        if (!validStatuses.contains(status)) {
            throw new InvalidStatusException("유효하지 않은 status 값입니다.");
        }
        Pageable pageable = PageRequest.of(page, size);
        TrashcanDetailsPageResponse response = trashcanService.getTrashcanDetailsByStatus(status, pageable, sort);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/trashcans/status/{id}")
    @Operation(summary = "쓰레기통 상태 변경",
            description = "쓰레기통 상태 변경, 쓰레기통 상태(ADDED, REGISTERED, SUGGESTED, REMOVED). 이 작업은 관리자만 수행할 수 있습니다.")
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
        List<String> validStatuses = Arrays.asList("added", "REGISTERED", "SUGGESTED", "REMOVED");
        if (!validStatuses.contains(request.getStatus())) {
            throw new InvalidStatusException("유효하지 않은 status 값입니다.");
        }
        trashcanService.updateTrashcanStatus(id, request.getStatus());
        return ResponseEntity.ok().body(new TrashcanMessageResponse("성공적으로 쓰레기통의 상태를 변경했습니다."));
    }


}
