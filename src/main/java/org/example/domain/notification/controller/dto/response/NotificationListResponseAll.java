package org.example.domain.notification.controller.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.domain.notification.domain.Notification;
import org.example.domain.notification.domain.NotificationType;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class NotificationListResponseAll {
    private Integer totalPages;
    private List<NotificationInfo> notificationInfoList;

    @Getter
    @AllArgsConstructor
    private class NotificationInfo {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime createdAt;
        private String state;
    }

    public NotificationListResponseAll(Page<Notification> responseData) {
        this.notificationInfoList = new ArrayList<>();
        this.totalPages = responseData.getTotalPages();
        for (Notification notification : responseData) {
            NotificationInfo notificationInfo = new NotificationInfo(notification.getId(),
                    notification.getTitle(), notification.getDescription(), notification.getCreatedAt(),
                    notification.getState());
            this.notificationInfoList.add(notificationInfo);
        }
    }

}
