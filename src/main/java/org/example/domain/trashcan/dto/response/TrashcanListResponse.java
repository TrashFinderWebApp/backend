package org.example.domain.trashcan.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TrashcanListResponse {
    private Long trashcanId;
    private String address;
    private String addressDetail;
    private List<String> imageUrls;
    private List<String> description;
    private Integer views;
    private String status;
    private Integer count; // 등록상태나 제안 상태인 경우의 횟수
    private Integer reportCount; // 추가: 신고 횟수
}
