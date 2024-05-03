package org.example.domain.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    UPDATED("업데이트"), GENERAL("일반"), EVENT("이벤트");

    private String type;

    NotificationType(String type) {
        this.type = type;
    }

}
