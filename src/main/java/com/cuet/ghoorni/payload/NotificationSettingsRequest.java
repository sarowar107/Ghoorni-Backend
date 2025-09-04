package com.cuet.ghoorni.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {
    private Boolean pushNotificationsEnabled;
    private Boolean noticeNotifications;
    private Boolean fileNotifications;
    private Boolean questionNotifications;
    private Boolean answerNotifications;
}
