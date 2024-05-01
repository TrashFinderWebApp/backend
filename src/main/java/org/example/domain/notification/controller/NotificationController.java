package org.example.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.domain.member.dto.response.ErrorMessage;
import org.example.domain.notification.controller.dto.NotificationListResponseAll;
import org.example.domain.notification.service.NotificationService;
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
    @Operation(summary = "공지사항 조회", description = "공지사항 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResponseAll.class))),
            @ApiResponse(responseCode = "400", description = "1. 이메일 중복 \t\n 2. 비밀번호 불일치 \t\n "
                    + "3. 이메일 혹은 비밀번호 형식이 맞지 않습니다. \t\n 4. 이메일, 비밀번호, 이름이 비어 있습니다. \t\n"
                    + "5. 인증 코드가 잘못 되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getNotificationList() {
        try {
            List<NotificationListResponseAll> responseList = notificationService.getNotificationList();
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
