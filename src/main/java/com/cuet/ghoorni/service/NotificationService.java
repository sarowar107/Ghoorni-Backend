package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Notification;
import com.cuet.ghoorni.model.NotificationSettings;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.NotificationResponse;
import com.cuet.ghoorni.payload.NotificationSettingsRequest;
import com.cuet.ghoorni.payload.NotificationSettingsResponse;
import com.cuet.ghoorni.repository.NotificationRepository;
import com.cuet.ghoorni.repository.NotificationSettingsRepository;
import com.cuet.ghoorni.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    /**
     * Create a new notification for a user
     */
    @Transactional
    public Notification createNotification(User recipient, String title, String message,
            Notification.NotificationType type, String referenceId) {
        // Check if user has notifications enabled for this type
        NotificationSettings settings = getOrCreateNotificationSettings(recipient);

        boolean shouldCreateNotification = switch (type) {
            case NOTICE_CREATED -> settings.getNoticeNotifications();
            case QUESTION_ASKED, QUESTION_ANSWERED -> settings.getQuestionNotifications();
            case FILE_UPLOADED -> settings.getFileNotifications();
        };

        if (!shouldCreateNotification || !settings.getPushNotificationsEnabled()) {
            return null; // Don't create notification if disabled
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get paginated notifications for a user
     */
    public Page<NotificationResponse> getUserNotifications(String userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable);

        return notifications.map(NotificationResponse::fromNotification);
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository
                .findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count for a user
     */
    public long getUnreadNotificationCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public boolean markAsRead(Long notificationId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int updated = notificationRepository.markAsRead(notificationId, user);
        return updated > 0;
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.markAllAsRead(user);
    }

    /**
     * Get user's notification settings
     */
    public NotificationSettingsResponse getNotificationSettings(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = getOrCreateNotificationSettings(user);
        return NotificationSettingsResponse.fromNotificationSettings(settings);
    }

    /**
     * Update user's notification settings
     */
    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(String userId, NotificationSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationSettings settings = getOrCreateNotificationSettings(user);

        if (request.getPushNotificationsEnabled() != null) {
            settings.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
        }
        if (request.getNoticeNotifications() != null) {
            settings.setNoticeNotifications(request.getNoticeNotifications());
        }
        if (request.getFileNotifications() != null) {
            settings.setFileNotifications(request.getFileNotifications());
        }
        if (request.getQuestionNotifications() != null) {
            settings.setQuestionNotifications(request.getQuestionNotifications());
        }
        if (request.getAnswerNotifications() != null) {
            settings.setAnswerNotifications(request.getAnswerNotifications());
        }

        NotificationSettings saved = notificationSettingsRepository.save(settings);
        return NotificationSettingsResponse.fromNotificationSettings(saved);
    }

    /**
     * Get or create default notification settings for a user
     */
    private NotificationSettings getOrCreateNotificationSettings(User user) {
        return notificationSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    NotificationSettings defaultSettings = NotificationSettings.builder()
                            .user(user)
                            .pushNotificationsEnabled(true)
                            .noticeNotifications(true)
                            .fileNotifications(true)
                            .questionNotifications(true)
                            .answerNotifications(true)
                            .build();
                    return notificationSettingsRepository.save(defaultSettings);
                });
    }

    /**
     * Delete notifications by reference ID (when content is deleted)
     */
    @Transactional
    public int deleteNotificationsByReferenceId(String referenceId, Notification.NotificationType type) {
        return notificationRepository.deleteByReferenceIdAndType(referenceId, type);
    }

    /**
     * Delete notifications by reference ID and type
     */
    @Transactional
    public int deleteNotificationsByReferenceId(String referenceId) {
        return notificationRepository.deleteByReferenceId(referenceId);
    }

    /**
     * Clean up old read notifications (can be called by a scheduled task)
     */
    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldReadNotifications(cutoffDate);
    }
}
