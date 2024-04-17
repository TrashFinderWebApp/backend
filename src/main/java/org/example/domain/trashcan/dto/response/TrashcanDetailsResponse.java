package org.example.domain.trashcan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;

@AllArgsConstructor
@Getter
public class TrashcanDetailsResponse {
    private Long trashcanId;
    private String address;
    private String addressDetail;
    private List<String> imageUrls;
    private List<String> description;
    private Integer views;

    @Schema(description = "기본값 0, 쓰레기통이 등록상태나 제안 상태인 경우 횟수")
    private Integer count;

}
