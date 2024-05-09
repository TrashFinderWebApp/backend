package org.example.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.domain.notification.controller.dto.request.CreateNotificationRequest;
import org.example.domain.notification.controller.dto.request.UpdateNotificationRequest;
import org.example.domain.notification.controller.dto.response.NotificationListResponseAll;
import org.example.domain.notification.domain.NotificationType;
import org.example.domain.notification.service.NotificationService;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.jwt.JwtProvider;
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

@Tag(name = "notification", description = "공지사항 API")
@AllArgsConstructor
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtProvider jwtProvider;


    @PostMapping("/")
    @Operation(summary = "공지사항 생성", description = "공지사항 생성 API, 관리자만 접근 가능합니다. \t\n "
            + "UPDATED, GENERAL, EVENT 상태값만 허용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "400", description = "1. 제목 미입력 \t\n 2. 공지사항 분류 미선택",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    })
    @Parameter(name = "access token")
    public ResponseEntity<?> createNotification(CreateNotificationRequest notificationRequest) {
        try {
            notificationService.createNotification(notificationRequest);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping
    @Operation(summary = "공지사항 조회", description = "공지사항 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "404", description = "공지사항이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getNotificationList(
            @RequestParam(defaultValue = "ALL", required = false) String notificationType) {
        try {
            List<NotificationListResponseAll> responseList;

            if (notificationType.equals("ALL")) {
                responseList = notificationService.getAllNotificationList();
            }
            else {
                responseList = notificationService.getStateNotificationList(
                        NotificationType.valueOf(notificationType));
            }
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("기타 서버 에러"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "공지사항 업데이트", description = "공지사항 업데이트 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "해당 공지사항을 찾지 못했습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> updateNotification(@PathVariable("id") Long id, @RequestBody UpdateNotificationRequest updateRequest) {
        try {
            notificationService.updateNotification(id, updateRequest);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공지사항 삭제", description = "공지사항 삭제 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id) {
        try {
            notificationService.deleteNotification(id);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
