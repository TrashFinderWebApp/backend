package org.example.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.domain.notification.controller.dto.NotificationListResponseAll;
import org.example.domain.notification.domain.NotificationType;
import org.example.domain.notification.service.NotificationService;
import org.example.global.advice.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notification", description = "공지사항 API")
@AllArgsConstructor
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "공지사항 전체 조회", description = "공지사항 전체 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "404", description = "공지사항이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getNotificationList() {
        try {
            List<NotificationListResponseAll> responseList = notificationService.getAllNotificationList();
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("기타 서버 에러"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list/update")
    @Operation(summary = "공지사항 업데이트 항목 조회", description = "공지사항 업데이트 항목 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "404", description = "공지사항 업데이트 항목이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getUpdatedNotificationList() {
        try {
            List<NotificationListResponseAll> responseList = notificationService.getStateNotificationList(
                    NotificationType.UPDATED);
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("기타 서버 에러"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list/general")
    @Operation(summary = "공지사항 일반 항목 조회", description = "공지사항 일반 항목 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "404", description = "공지사항 일반 항목이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getGeneralNotificationList() {
        try {
            List<NotificationListResponseAll> responseList = notificationService.getStateNotificationList(
                    NotificationType.GENERAL);
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("기타 서버 에러"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list/event")
    @Operation(summary = "공지사항 이벤트 항목 조회", description = "공지사항 이벤트 항목 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "404", description = "이벤트 항목 공지사항이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "기타 서버 에러",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getGeneraEventList() {
        try {
            List<NotificationListResponseAll> responseList = notificationService.getStateNotificationList(
                    NotificationType.EVENT);
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("기타 서버 에러"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
