package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.Files;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileResponse {
    private Long fileId;
    private String topic;
    private String content;
    private LocalDateTime uploadedAt;
    private boolean isPublic;
    private UserInfo uploadedBy;

    public static FileResponse fromEntity(Files file) {
        FileResponse response = new FileResponse();
        response.setFileId(file.getFileId());
        response.setTopic(file.getTopic());
        response.setContent(file.getContent());
        response.setUploadedAt(file.getUploadedAt());
        response.setPublic(file.isPublic());

        response.setUploadedBy(new UserInfo(
                file.getUploadedBy().getUserId(),
                file.getUploadedBy().getName(),
                file.getUploadedBy().getRole(),
                file.getUploadedBy().getDeptName(),
                file.getUploadedBy().getBatch()));

        return response;
    }
}
