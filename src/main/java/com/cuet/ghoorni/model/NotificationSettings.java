package com.cuet.ghoorni.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_settings")
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long settingsId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "push_notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;

    @Column(name = "notice_notifications", nullable = false)
    @Builder.Default
    private Boolean noticeNotifications = true;

    @Column(name = "file_notifications", nullable = false)
    @Builder.Default
    private Boolean fileNotifications = true;

    @Column(name = "question_notifications", nullable = false)
    @Builder.Default
    private Boolean questionNotifications = true;

    @Column(name = "answer_notifications", nullable = false)
    @Builder.Default
    private Boolean answerNotifications = true;
}
