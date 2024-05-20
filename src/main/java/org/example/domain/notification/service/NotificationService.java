package org.example.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.notification.controller.dto.request.CreateNotificationRequest;
import org.example.domain.notification.controller.dto.request.UpdateNotificationRequest;
import org.example.domain.notification.controller.dto.response.NotificationListResponseAll;
import org.example.domain.notification.domain.Notification;
import org.example.domain.notification.domain.NotificationType;
import org.example.domain.notification.repository.NotificationRepository;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JwtProvider jwtProvider;

    public void createNotification(CreateNotificationRequest notificationRequest) {
        log.info(notificationRequest.toString());
        notificationRepository.save(
                new Notification(
                        notificationRequest.getTitle(),
                        notificationRequest.getDescription(), notificationRequest.getState()
                ));
    }

    public NotificationListResponseAll getAllNotificationList(Integer page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Notification> responseData = notificationRepository.findAllByOrderByCreatedAtDesc(pageRequest);
        return convertToResponseAllList(responseData);
    }

    public NotificationListResponseAll getStateNotificationList(String notificationType, Integer page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Notification> responseData = notificationRepository.findByStateOrderByCreatedAtDesc(notificationType, pageRequest);
        return convertToResponseAllList(responseData);
    }


    private NotificationListResponseAll convertToResponseAllList(Page<Notification> responseData) {
        return new NotificationListResponseAll(responseData);
    }

    @Transactional
    public void updateNotification(Long id, UpdateNotificationRequest updateRequest) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾지 못했습니다."));
        notification.update(updateRequest);
        //notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
