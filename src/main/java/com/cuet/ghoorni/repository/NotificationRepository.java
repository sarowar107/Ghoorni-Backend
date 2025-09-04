package com.cuet.ghoorni.repository;

import com.cuet.ghoorni.model.Notification;
import com.cuet.ghoorni.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by recipient, ordered by creation date (newest first)
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    // Find unread notifications by recipient
    List<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);

    // Count unread notifications for a user
    long countByRecipientAndIsReadFalse(User recipient);

    // Mark notification as read
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.notificationId = :notificationId AND n.recipient = :recipient")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("recipient") User recipient);

    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsRead(@Param("recipient") User recipient);

    // Delete notifications by reference ID (when content is deleted)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.referenceId = :referenceId")
    int deleteByReferenceId(@Param("referenceId") String referenceId);

    // Delete notifications by reference ID and type
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.referenceId = :referenceId AND n.type = :type")
    int deleteByReferenceIdAndType(@Param("referenceId") String referenceId,
            @Param("type") Notification.NotificationType type);

    // Delete old read notifications (older than specified days)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
