package org.example.domain.notification.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateNotificationRequest {
    private String title;
    private String description;
    private String state;
}
