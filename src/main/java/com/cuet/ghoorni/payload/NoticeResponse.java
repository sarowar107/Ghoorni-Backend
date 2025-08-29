package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.Notice;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeResponse {
    private Long noticeId;
    private String title;
    private String content;
    private boolean isPublic;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
    private UserInfo createdBy;

    @Data
    public static class UserInfo {
        private String userId;
        private String name;
        private String role;
        private String deptName;
        private String batch;

        public UserInfo(String userId, String name, String role, String deptName, String batch) {
            this.userId = userId;
            this.name = name;
            this.role = role;
            this.deptName = deptName;
            this.batch = batch;
        }
    }

    public static NoticeResponse fromEntity(Notice notice) {
        NoticeResponse response = new NoticeResponse();
        response.setNoticeId(notice.getNoticeId());
        response.setTitle(notice.getTitle());
        response.setContent(notice.getContent());
        response.setPublic(notice.isPublic());
        response.setExpiryTime(notice.getExpiryTime());
        response.setCreatedAt(notice.getCreatedAt());

        // Map only the necessary user information
        response.setCreatedBy(new UserInfo(
                notice.getCreatedBy().getUserId(),
                notice.getCreatedBy().getName(),
                notice.getCreatedBy().getRole(),
                notice.getCreatedBy().getDeptName(),
                notice.getCreatedBy().getBatch()));

        return response;
    }
}
