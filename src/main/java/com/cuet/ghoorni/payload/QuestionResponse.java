package com.cuet.ghoorni.payload;

import com.cuet.ghoorni.model.Question;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionResponse {
    private Long questionId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String toDept;
    private String toBatch;
    private UserInfo askedBy;
    private List<AnswerResponse> answers;

    public static QuestionResponse fromEntity(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setQuestionId(question.getQuestionId());
        response.setTitle(question.getTitle());
        response.setDescription(question.getDescription());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        response.setToDept(question.getToDept());
        response.setToBatch(question.getToBatch());

        response.setAskedBy(new UserInfo(
                question.getAskedBy().getUserId(),
                question.getAskedBy().getName(),
                question.getAskedBy().getRole(),
                question.getAskedBy().getDeptName(),
                question.getAskedBy().getBatch()));

        if (question.getAnswers() != null) {
            response.setAnswers(question.getAnswers().stream()
                    .map(AnswerResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
