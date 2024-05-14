package org.example.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.domain.admin.controller.dto.response.MemberListResponse;
import org.example.domain.admin.service.AdminService;
import org.example.domain.notification.controller.dto.request.CreateNotificationRequest;
import org.example.domain.notification.controller.dto.response.NotificationListResponseAll;
import org.example.global.advice.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "admin", description = "관리자 API")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/members")
    @Operation(summary = "멤버 리스트 조회", description = "멤버 리스트 API, 관리자만 접근 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token", in = ParameterIn.HEADER)
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
}