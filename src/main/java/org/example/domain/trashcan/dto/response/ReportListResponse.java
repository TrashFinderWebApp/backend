package org.example.domain.trashcan.dto.response;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportListResponse {
    private Integer totalPages;
    private List<ReportResponse> reportResponses;
}
