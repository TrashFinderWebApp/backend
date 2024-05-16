package org.example.domain.trashcan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrashcanReportRequest {

    @NotBlank(message = "구체적인 신고사항을 입력해주세요.")
    private String description;
}
