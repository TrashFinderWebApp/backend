package org.example.domain.notification.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.notification.controller.dto.NotificationListResponseAll;
import org.example.domain.notification.domain.Notification;
import org.example.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationListResponseAll> getNotificationList() {
        List<Notification> responseList = notificationRepository.findAll();
        return null;
    }
}
