package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Answer;
import com.cuet.ghoorni.payload.AnswerRequest;
import com.cuet.ghoorni.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping("/submit")
    public ResponseEntity<Answer> submitAnswer(@RequestBody AnswerRequest answerRequest, Authentication authentication) {
        Answer newAnswer = answerService.submitAnswer(answerRequest, authentication.getName());
        return new ResponseEntity<>(newAnswer, HttpStatus.CREATED);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<Answer>> getAnswersByQuestionId(@PathVariable Long questionId) {
        List<Answer> answers = answerService.findAnswersByQuestionId(questionId);
        return new ResponseEntity<>(answers, HttpStatus.OK);
    }
}