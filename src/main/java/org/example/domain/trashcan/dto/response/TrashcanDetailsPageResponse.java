package org.example.domain.trashcan.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TrashcanDetailsPageResponse {
    private Integer totalPages;
    private List<TrashcanListResponse> trashcanDetailsResponseWithReportCounts;

}
