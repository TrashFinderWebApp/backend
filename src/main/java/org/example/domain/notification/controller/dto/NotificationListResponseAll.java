package org.example.domain.notification.controller.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationListResponseAll {
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
