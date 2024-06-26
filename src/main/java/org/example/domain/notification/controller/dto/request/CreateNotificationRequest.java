package org.example.domain.notification.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String description;

    @NotBlank(message = "공지사항 분류를 선택해주세요.")
    @Schema(name = "상태값", description = "UPDATED, GENERAL, EVENT 상태값만 허용")
    private String state;
}
