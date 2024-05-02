package org.example.domain.trashcan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class TrashcanLocationResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String addressDetail;
    private Integer views;

    @Schema(description = "기본값 0, 쓰레기통이 등록상태나 제안 상태인 경우 횟수")
    private Integer count;
}
