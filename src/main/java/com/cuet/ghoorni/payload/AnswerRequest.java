package com.cuet.ghoorni.payload;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long questionId;
    private String content;
}