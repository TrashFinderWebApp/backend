package org.example.domain.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    UPDATED, GENERAL, EVENT;

    private String type;

}
