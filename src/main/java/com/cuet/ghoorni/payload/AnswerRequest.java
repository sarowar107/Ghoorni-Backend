package com.cuet.ghoorni.payload;

public class AnswerRequest {
    private Long questionId;
    private String content;

    public AnswerRequest() {
    }

    public AnswerRequest(Long questionId, String content) {
        this.questionId = questionId;
        this.content = content;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}