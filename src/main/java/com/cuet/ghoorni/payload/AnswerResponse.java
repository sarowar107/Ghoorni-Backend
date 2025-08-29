package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.Answer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerResponse {
    private Long ansId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfo answeredBy;
    private Long questionId;

    public static AnswerResponse fromEntity(Answer answer) {
        AnswerResponse response = new AnswerResponse();
        response.setAnsId(answer.getAnsId());
        response.setContent(answer.getContent());
        response.setCreatedAt(answer.getCreatedAt());
        response.setUpdatedAt(answer.getUpdatedAt());
        response.setQuestionId(answer.getQuestion().getQuestionId());

        response.setAnsweredBy(new UserInfo(
                answer.getAnsweredBy().getUserId(),
                answer.getAnsweredBy().getName(),
                answer.getAnsweredBy().getRole(),
                answer.getAnsweredBy().getDeptName(),
                answer.getAnsweredBy().getBatch()));

        return response;
    }
}
