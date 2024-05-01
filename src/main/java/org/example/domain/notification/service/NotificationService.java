package org.example.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.notification.controller.dto.request.CreateNotificationRequest;
import org.example.domain.notification.controller.dto.response.NotificationListResponseAll;
import org.example.domain.notification.domain.Notification;
import org.example.domain.notification.domain.NotificationType;
import org.example.domain.notification.repository.NotificationRepository;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JwtProvider jwtProvider;

    public void createNotification(CreateNotificationRequest notificationRequest) {
        notificationRepository.save(
                new Notification(
                        notificationRequest.getTitle(),
                        notificationRequest.getDescription(),
                        NotificationType.valueOf(notificationRequest.getState())
                ));
    }

    public List<NotificationListResponseAll> getAllNotificationList() {
        List<Notification> responseData = notificationRepository.findAllByOrderByCreatedAtDesc();
        List<NotificationListResponseAll> responseAllList = convertToResponseAllList(responseData);

        if (responseAllList.isEmpty()) {
            throw new IllegalArgumentException("resources not found.");
        }
        return responseAllList;
    }

    public List<NotificationListResponseAll> getStateNotificationList(NotificationType type) {
        List<Notification> responseData = notificationRepository.findByStateOrderByCreatedAtDesc(type);
        List<NotificationListResponseAll> responseAllList = convertToResponseAllList(responseData);

        if (responseAllList.isEmpty()) {
            throw new IllegalArgumentException("resources not found.");
        }
        return responseAllList;
    }

    private List<NotificationListResponseAll> convertToResponseAllList(List<Notification> responseData) {
        List<NotificationListResponseAll> allList = new ArrayList<>();

        for (Notification notification : responseData) {
            NotificationListResponseAll oneNotification = new NotificationListResponseAll(
                    notification.getTitle(), notification.getDescription(), notification.getCreatedAt());
            allList.add(oneNotification);
        }

        return allList;
    }
}
