package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.payload.NotificationResponse;
import com.cuet.ghoorni.payload.NotificationSettingsRequest;
import com.cuet.ghoorni.payload.NotificationSettingsResponse;
import com.cuet.ghoorni.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get paginated notifications for the current user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = authentication.getName();
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for the current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count for the current user
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean success = notificationService.markAsRead(notificationId, userId);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to mark notification as read"));
        }
    }

    /**
     * Mark all notifications as read for the current user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read",
                "updatedCount", updatedCount));
    }

    /**
     * Get notification settings for the current user
     */
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings(Authentication authentication) {
        String userId = authentication.getName();
        NotificationSettingsResponse settings = notificationService.getNotificationSettings(userId);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update notification settings for the current user
     */
    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @RequestBody NotificationSettingsRequest request,
            Authentication authentication) {

        String userId = authentication.getName();
        NotificationSettingsResponse settings = notificationService.updateNotificationSettings(userId, request);
        return ResponseEntity.ok(settings);
    }
}
