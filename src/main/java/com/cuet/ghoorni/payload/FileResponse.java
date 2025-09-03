package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.Files;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileResponse {
    private Long fileId;
    private String topic;
    private String category;
    private String content;
    private LocalDateTime uploadedAt;
    private boolean isPublic;
    private String toDept;
    private String toBatch;
    private UserInfo uploadedBy;

    public static FileResponse fromEntity(Files file) {
        FileResponse response = new FileResponse();
        response.setFileId(file.getFileId());
        response.setTopic(file.getTopic());
        response.setCategory(file.getCategory());
        response.setContent(file.getContent());
        response.setUploadedAt(file.getUploadedAt());
        response.setPublic(file.isPublic());
        response.setToDept(file.getToDept());
        response.setToBatch(file.getToBatch());

        response.setUploadedBy(new UserInfo(
                file.getUploadedBy().getUserId(),
                file.getUploadedBy().getName(),
                file.getUploadedBy().getRole(),
                file.getUploadedBy().getDeptName(),
                file.getUploadedBy().getBatch()));

        return response;
    }
}
