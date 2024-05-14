package org.example.domain.trashcan.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.trashcan.domain.Report;

@AllArgsConstructor
@Getter
public class ReportResponse {
    private Long id;
    private LocalDateTime createdAt;
    private Long memberId;
    private String description;

    public ReportResponse(Report report) {
        this.id = report.getId();
        this.createdAt = report.getCreatedAt();
        this.memberId = report.getMember().getId();
        this.description = report.getDescription();
    }
}
