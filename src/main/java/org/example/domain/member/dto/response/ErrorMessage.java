package org.example.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorMessage {
    @Schema(description = "에러 메세지", example = "사용자 id가 존재하지 않습니다.")
    private String errorMessage;
}