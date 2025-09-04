package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.NotificationSettings;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsResponse {
    private Long settingsId;
    private String userId;
    private Boolean pushNotificationsEnabled;
    private Boolean noticeNotifications;
    private Boolean fileNotifications;
    private Boolean questionNotifications;
    private Boolean answerNotifications;

    public static NotificationSettingsResponse fromNotificationSettings(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .settingsId(settings.getSettingsId())
                .userId(settings.getUser().getUserId())
                .pushNotificationsEnabled(settings.getPushNotificationsEnabled())
                .noticeNotifications(settings.getNoticeNotifications())
                .fileNotifications(settings.getFileNotifications())
                .questionNotifications(settings.getQuestionNotifications())
                .answerNotifications(settings.getAnswerNotifications())
                .build();
    }
}
